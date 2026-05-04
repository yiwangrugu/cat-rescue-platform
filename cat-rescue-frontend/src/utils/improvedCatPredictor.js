import { getAdvancedKNNModel } from './advancedCatKNNModel'

let model = null

export const loadImprovedModel = () => {
  if (!model) {
    model = getAdvancedKNNModel()
  }
  return model
}

export const isImprovedModelAvailable = () => {
  try {
    const m = loadImprovedModel()
    return m && m.X_train_std && m.X_train_std.length > 0
  } catch (e) {
    return false
  }
}

const extractCatRegion = (imageData, width, height) => {
  const data = imageData.data
  const result = new Float32Array(data.length)

  const centerX = width / 2
  const centerY = height / 2
  const centerRadius = Math.min(width, height) * 0.35

  const centerPixels = []
  for (let y = 0; y < height; y++) {
    for (let x = 0; x < width; x++) {
      const dx = x - centerX
      const dy = y - centerY
      const distance = Math.sqrt(dx * dx + dy * dy)
      if (distance < centerRadius) {
        const idx = y * width + x
        const pixelIdx = idx * 4
        centerPixels.push({
          r: data[pixelIdx] / 255.0,
          g: data[pixelIdx + 1] / 255.0,
          b: data[pixelIdx + 2] / 255.0
        })
      }
    }
  }

  let centerAvgR = 0.5, centerAvgG = 0.5, centerAvgB = 0.5
  if (centerPixels.length > 0) {
    centerAvgR = centerPixels.reduce((sum, p) => sum + p.r, 0) / centerPixels.length
    centerAvgG = centerPixels.reduce((sum, p) => sum + p.g, 0) / centerPixels.length
    centerAvgB = centerPixels.reduce((sum, p) => sum + p.b, 0) / centerPixels.length
  }

  const maxDistance = Math.sqrt(centerX * centerX + centerY * centerY)

  for (let y = 0; y < height; y++) {
    for (let x = 0; x < width; x++) {
      const idx = y * width + x
      const pixelIdx = idx * 4

      const r = data[pixelIdx] / 255.0
      const g = data[pixelIdx + 1] / 255.0
      const b = data[pixelIdx + 2] / 255.0

      const dx = x - centerX
      const dy = y - centerY
      const distance = Math.sqrt(dx * dx + dy * dy)
      const normalizedDistance = distance / maxDistance

      let centerScore = 0
      if (normalizedDistance < 0.25) {
        centerScore = 1.0
      } else if (normalizedDistance < 0.45) {
        centerScore = 0.95
      } else if (normalizedDistance < 0.6) {
        centerScore = 0.85
      } else if (normalizedDistance < 0.75) {
        centerScore = 0.65
      } else if (normalizedDistance < 0.85) {
        centerScore = 0.45
      } else {
        centerScore = 0.25
      }

      const gray = (r + g + b) / 3
      const centerGray = (centerAvgR + centerAvgG + centerAvgB) / 3
      const grayDiff = Math.abs(gray - centerGray)

      const colorDiff = Math.abs(r - centerAvgR) + Math.abs(g - centerAvgG) + Math.abs(b - centerAvgB)

      const maxRGB = Math.max(r, g, b)
      const minRGB = Math.min(r, g, b)
      const saturation = maxRGB > 0 ? (maxRGB - minRGB) / maxRGB : 0

      const grayScore = Math.max(0, 1 - grayDiff * 2)
      const colorScore = Math.max(0, 1 - colorDiff / 2)

      const foregroundScore = centerScore * 0.5 + grayScore * 0.25 + colorScore * 0.2 + saturation * 0.05
      const clippedScore = Math.max(0, Math.min(1, foregroundScore))

      result[pixelIdx] = clippedScore * r + (1 - clippedScore) * 0.5
      result[pixelIdx + 1] = clippedScore * g + (1 - clippedScore) * 0.5
      result[pixelIdx + 2] = clippedScore * b + (1 - clippedScore) * 0.5
      result[pixelIdx + 3] = data[pixelIdx + 3]
    }
  }

  return result
}

const extractImprovedFeatures = (imageElement) => {
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

    const region = extractCatRegion(imageData, size, size)

    const imgArray = []
    const grayArray = []
    for (let i = 0; i < data.length; i += 4) {
      const r = region[i]
      const g = region[i + 1]
      const b = region[i + 2]

      imgArray.push([r, g, b])
      grayArray.push((r + g + b) / 3)
    }

    const features = []

    const rValues = imgArray.map(p => p[0])
    const gValues = imgArray.map(p => p[1])
    const bValues = imgArray.map(p => p[2])

    const avgR = rValues.reduce((a, b) => a + b, 0) / rValues.length
    const avgG = gValues.reduce((a, b) => a + b, 0) / gValues.length
    const avgB = bValues.reduce((a, b) => a + b, 0) / bValues.length

    const stdR = Math.sqrt(rValues.reduce((sum, val) => sum + (val - avgR) ** 2, 0) / rValues.length)
    const stdG = Math.sqrt(gValues.reduce((sum, val) => sum + (val - avgG) ** 2, 0) / gValues.length)
    const stdB = Math.sqrt(bValues.reduce((sum, val) => sum + (val - avgB) ** 2, 0) / bValues.length)

    const sortedR = [...rValues].sort((a, b) => a - b)
    const sortedG = [...gValues].sort((a, b) => a - b)
    const sortedB = [...bValues].sort((a, b) => a - b)

    const medianR = sortedR[Math.floor(sortedR.length / 2)]
    const medianG = sortedG[Math.floor(sortedG.length / 2)]
    const medianB = sortedB[Math.floor(sortedB.length / 2)]

    const p10R = sortedR[Math.floor(sortedR.length * 0.1)]
    const p10G = sortedG[Math.floor(sortedG.length * 0.1)]
    const p10B = sortedB[Math.floor(sortedB.length * 0.1)]

    const p90R = sortedR[Math.floor(sortedR.length * 0.9)]
    const p90G = sortedG[Math.floor(sortedG.length * 0.9)]
    const p90B = sortedB[Math.floor(sortedB.length * 0.9)]

    features.push(avgR, avgG, avgB, stdR, stdG, stdB, medianR, medianG, medianB, p10R, p10G, p10B, p90R, p90G, p90B)

    const grayValues = imgArray.map(p => (p[0] + p[1] + p[2]) / 3)
    const avgBrightness = grayValues.reduce((a, b) => a + b, 0) / grayValues.length
    const stdBrightness = Math.sqrt(grayValues.reduce((sum, val) => sum + (val - avgBrightness) ** 2, 0) / grayValues.length)
    const sortedGray = [...grayValues].sort((a, b) => a - b)
    const medianBrightness = sortedGray[Math.floor(sortedGray.length / 2)]

    const darkPixels = grayValues.filter(v => v < 0.3).length
    const lightPixels = grayValues.filter(v => v > 0.7).length
    const totalPixels = grayValues.length

    features.push(avgBrightness, stdBrightness, medianBrightness, darkPixels / totalPixels, lightPixels / totalPixels)

    const orangePixels = imgArray.filter(p => p[0] > 0.6 && p[1] > 0.3 && p[1] < 0.7 && p[2] < 0.5).length
    features.push(orangePixels / totalPixels)

    const blackPixels = imgArray.filter(p => p[0] < 0.2 && p[1] < 0.2 && p[2] < 0.2).length
    features.push(blackPixels / totalPixels)

    const whitePixels = imgArray.filter(p => p[0] > 0.8 && p[1] > 0.8 && p[2] > 0.8).length
    features.push(whitePixels / totalPixels)

    for (let i = 0; i < 3; i++) {
      for (let j = 0; j < 3; j++) {
        if (i !== j) {
          const channel1 = imgArray.map(p => p[i])
          const channel2 = imgArray.map(p => p[j])
          const mean1 = channel1.reduce((a, b) => a + b, 0) / channel1.length
          const mean2 = channel2.reduce((a, b) => a + b, 0) / channel2.length
          const cov = channel1.reduce((sum, val, idx) => sum + (val - mean1) * (channel2[idx] - mean2), 0) / channel1.length
          features.push(cov)
        }
      }
    }

    for (const scale of [0.25, 0.5]) {
      const smallSize = Math.floor(size * scale)
      const smallCanvas = document.createElement('canvas')
      smallCanvas.width = smallSize
      smallCanvas.height = smallSize
      const smallCtx = smallCanvas.getContext('2d')
      smallCtx.drawImage(canvas, 0, 0, smallSize, smallSize)
      const smallData = smallCtx.getImageData(0, 0, smallSize, smallSize).data

      for (let c = 0; c < 3; c++) {
        let sum = 0
        for (let i = c; i < smallData.length; i += 4) {
          sum += smallData[i] / 255.0
        }
        features.push(sum / (smallSize * smallSize))
      }
    }

    resolve(features)
  })
}

const standardizeFeatures = (features, mean, std) => {
  return features.map((val, idx) => (val - mean[idx]) / std[idx])
}

const predictKNNBasic = (x_std, X_train, y_train, k, weights, metric, idx_to_breed) => {
  if (!X_train || X_train.length === 0) {
    return { sortedVotes: [['黑猫', 1]], minDistance: 1 }
  }

  const distances = []
  for (let i = 0; i < X_train.length; i++) {
    let dist = 0
    const trainSample = X_train[i]
    if (!trainSample || trainSample.length !== x_std.length) {
      continue
    }
    if (metric === 'euclidean') {
      for (let j = 0; j < x_std.length; j++) {
        const diff = x_std[j] - trainSample[j]
        dist += diff * diff
      }
      dist = Math.sqrt(dist)
    } else {
      for (let j = 0; j < x_std.length; j++) {
        dist += Math.abs(x_std[j] - trainSample[j])
      }
    }
    if (!isNaN(dist) && isFinite(dist)) {
      distances.push({ index: i, distance: dist })
    }
  }

  if (distances.length === 0) {
    return { sortedVotes: [['黑猫', 1]], minDistance: 1 }
  }

  distances.sort((a, b) => a.distance - b.distance)
  const nearest = distances.slice(0, k)

  const voteCount = {}
  let minDist = Infinity
  for (const n of nearest) {
    const labelIdx = y_train[n.index]
    const label = idx_to_breed ? (idx_to_breed[labelIdx] || idx_to_breed[String(labelIdx)]) : labelIdx

    if (label !== undefined && label !== null) {
      const weight = weights === 'distance' ? (1 / (n.distance + 1e-6)) : 1
      voteCount[label] = (voteCount[label] || 0) + weight
      if (n.distance < minDist) {
        minDist = n.distance
      }
    }
  }

  if (Object.keys(voteCount).length === 0) {
    return { sortedVotes: [['黑猫', 1]], minDistance: 1 }
  }

  const sortedVotes = Object.entries(voteCount).sort((a, b) => b[1] - a[1])
  minDist = isFinite(minDist) ? minDist : 1

  return { sortedVotes, minDistance: minDist }
}

const predictWithKNN = (x_std, model) => {
  const X_train_std = model.X_train_std
  const y_train = model.y_train
  const k = model.best_k || 1
  const metric = model.best_metric || 'euclidean'
  const weights = model.best_weights || 'uniform'
  const idx_to_breed = model.idx_to_breed

  if (!X_train_std || X_train_std.length === 0) {
    console.error('训练数据为空')
    return { sortedVotes: [['黑猫', 1]], minDistance: 1 }
  }

  return predictKNNBasic(x_std, X_train_std, y_train, k, weights, metric, idx_to_breed)
}

export const predictCatBreedWithImprovedModel = async (imageElement, topN = 3) => {
  const m = loadImprovedModel()
  if (!m) {
    return null
  }

  const features = await extractImprovedFeatures(imageElement)

  if (!features || features.length === 0) {
    console.error('特征提取失败')
    return null
  }

  const x_std = standardizeFeatures(features, m.mean, m.std)

  if (!x_std || x_std.length === 0) {
    console.error('特征标准化失败')
    return null
  }

  const result = predictWithKNN(x_std, m)

  if (!result || !result.sortedVotes || result.sortedVotes.length === 0) {
    console.error('KNN预测失败')
    return null
  }

  const { sortedVotes, minDistance } = result

  const primaryBreed = sortedVotes[0][0]
  const primaryVotes = sortedVotes[0][1]
  const totalVotes = sortedVotes.reduce((sum, [, votes]) => sum + votes, 0)

  if (totalVotes === 0) {
    console.error('总票数为0')
    return null
  }

  let confidence = primaryVotes / totalVotes

  confidence = Math.pow(confidence, 0.7)

  if (minDistance !== undefined && minDistance !== null && !isNaN(minDistance)) {
    const distanceFactor = Math.max(0.7, 1 - minDistance / 3)
    confidence = confidence * 0.85 + distanceFactor * 0.15
  }

  confidence = Math.max(0.5, Math.min(0.995, confidence))

  const alternativeBreeds = sortedVotes.slice(1, topN).map(([breed, votes], index) => {
    const voteRatio = votes / totalVotes
    const boostedRatio = Math.pow(voteRatio, 0.6)
    const altConfidence = Math.max(0.35, Math.min(0.95, boostedRatio * 0.95))
    return {
      breed,
      confidence: altConfidence
    }
  })

  return {
    primaryBreed: {
      breed: primaryBreed,
      confidence: confidence
    },
    alternativeBreeds
  }
}
