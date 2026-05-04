import { getKNNModel } from './catKNNModel'

let knnModel = null

export const loadKNNModel = () => {
  if (!knnModel) {
    knnModel = getKNNModel()
  }
  return knnModel
}

export const isKNNModelAvailable = () => {
  try {
    const model = loadKNNModel()
    return model && model.X_train_features && model.X_train_features.length > 0
  } catch (e) {
    return false
  }
}

const extractFeaturesForKNN = (imageElement) => {
  return new Promise((resolve) => {
    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')
    const size = 224

    canvas.width = size
    canvas.height = size

    const ratio = Math.min(size / imageElement.width, size / imageElement.height)
    const newWidth = imageElement.width * ratio
    const newHeight = imageElement.height * ratio
    const x = (size - newWidth) / 2
    const y = (size - newHeight) / 2

    ctx.fillStyle = '#808080'
    ctx.fillRect(0, 0, size, size)
    ctx.drawImage(imageElement, x, y, newWidth, newHeight)

    const imageData = ctx.getImageData(0, 0, size, size)
    const data = imageData.data

    let totalR = 0, totalG = 0, totalB = 0
    let darkPixels = 0, lightPixels = 0, orangePixels = 0
    const pixels = []

    for (let i = 0; i < data.length; i += 4) {
      const r = data[i] / 255.0
      const g = data[i + 1] / 255.0
      const b = data[i + 2] / 255.0

      totalR += r
      totalG += g
      totalB += b

      const brightness = (r + g + b) / 3
      if (brightness < 0.3) darkPixels++
      if (brightness > 0.7) lightPixels++
      if (r > 0.6 && g > 0.3 && g < 0.7 && b < 0.5) orangePixels++

      pixels.push([r, g, b])
    }

    const pixelCount = size * size
    const avgR = totalR / pixelCount
    const avgG = totalG / pixelCount
    const avgB = totalB / pixelCount

    let stdR = 0, stdG = 0, stdB = 0
    for (const [r, g, b] of pixels) {
      stdR += (r - avgR) ** 2
      stdG += (g - avgG) ** 2
      stdB += (b - avgB) ** 2
    }
    stdR = Math.sqrt(stdR / pixelCount)
    stdG = Math.sqrt(stdG / pixelCount)
    stdB = Math.sqrt(stdB / pixelCount)

    const brightness = (avgR + avgG + avgB) / 3

    resolve([
      avgR, avgG, avgB,
      stdR, stdG, stdB,
      brightness,
      darkPixels / pixelCount,
      lightPixels / pixelCount,
      orangePixels / pixelCount
    ])
  })
}

export const predictCatBreedWithKNN = async (imageElement, k = null) => {
  const model = loadKNNModel()
  if (!model || !model.X_train_features) {
    return null
  }

  const useK = k || model.best_k || 3

  const features = await extractFeaturesForKNN(imageElement)
  const X_train = model.X_train_features
  const y_train = model.y_train
  const idx_to_breed = model.idx_to_breed

  const distances = X_train.map((trainFeat, idx) => {
    let dist = 0
    for (let i = 0; i < features.length; i++) {
      dist += (features[i] - trainFeat[i]) ** 2
    }
    return { distance: Math.sqrt(dist), label: y_train[idx] }
  })

  distances.sort((a, b) => a.distance - b.distance)

  const nearestNeighbors = distances.slice(0, useK)
  
  const voteCount = {}
  let minDistance = Infinity
  
  for (const neighbor of nearestNeighbors) {
    const breed = idx_to_breed[neighbor.label]
    voteCount[breed] = (voteCount[breed] || 0) + 1
    if (neighbor.distance < minDistance) {
      minDistance = neighbor.distance
    }
  }
  
  const sortedVotes = Object.entries(voteCount).sort((a, b) => b[1] - a[1])
  
  const confidence = Math.max(0, 1 - minDistance / 2)
  
  const primaryBreed = sortedVotes[0][0]
  
  const alternativeBreeds = sortedVotes.slice(1, 4).map(([breed, votes]) => ({
    breed,
    confidence: votes / useK * confidence
  }))

  return {
    primaryBreed: {
      breed: primaryBreed,
      confidence: Math.min(confidence, 0.95)
    },
    alternativeBreeds
  }
}
