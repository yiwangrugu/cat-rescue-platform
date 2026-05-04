import * as tf from '@tensorflow/tfjs'

let model = null
let modelMetadata = null
let isModelLoaded = false

const MODEL_PATH = '/models/sample_model/model.json'
const METADATA_PATH = '/models/metadata.json'

export const loadCatBreedModel = async () => {
  if (isModelLoaded && model) {
    return model
  }
  
  try {
    console.log('正在加载猫咪品种识别模型...')
    
    const metadataResponse = await fetch(METADATA_PATH)
    if (metadataResponse.ok) {
      modelMetadata = await metadataResponse.json()
      console.log('模型元数据已加载:', modelMetadata)
    }
    
    model = await tf.loadLayersModel(MODEL_PATH)
    isModelLoaded = true
    
    console.log('猫咪品种识别模型加载成功！')
    return model
  } catch (error) {
    console.error('加载模型失败:', error)
    console.log('将使用规则匹配作为备选方案')
    return null
  }
}

export const predictCatBreed = async (imageElement) => {
  if (!model || !isModelLoaded) {
    console.log('模型未加载，使用规则匹配')
    return null
  }
  
  try {
    return tf.tidy(() => {
      const imageSize = modelMetadata?.imageSize || [224, 224]
      
      let tensor = tf.browser.fromPixels(imageElement)
        .resizeNearestNeighbor(imageSize)
        .toFloat()
        .div(tf.scalar(255.0))
        .expandDims(0)
      
      const predictions = model.predict(tensor)
      const probabilities = Array.from(predictions.dataSync())
      
      const results = probabilities.map((prob, index) => ({
        breed: modelMetadata?.idx_to_breed?.[index] || `品种${index}`,
        confidence: prob
      }))
      
      results.sort((a, b) => b.confidence - a.confidence)
      
      return {
        primaryBreed: results[0],
        alternativeBreeds: results.slice(1, 4),
        allResults: results
      }
    })
  } catch (error) {
    console.error('预测失败:', error)
    return null
  }
}

export const getBreedList = () => {
  if (modelMetadata?.breeds) {
    return modelMetadata.breeds
  }
  return [
    '橘猫', '黑猫', '白猫', '狸花猫', '三花猫', 
    '玳瑁猫', '金渐层', '银渐层', '缅因猫', '布偶猫',
    '蓝猫', '奶牛猫'
  ]
}

export const isModelAvailable = () => {
  return isModelLoaded && model !== null
}

export const unloadModel = () => {
  if (model) {
    model.dispose()
    model = null
  }
  isModelLoaded = false
  modelMetadata = null
}
