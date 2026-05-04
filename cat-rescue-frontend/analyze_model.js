import { getAdvancedKNNModel } from './src/utils/advancedCatKNNModel.js';

const model = getAdvancedKNNModel();
const y_train = model.y_train;
const idx_to_breed = model.idx_to_breed;

console.log('训练数据总数:', y_train.length);
console.log('\n各品种分布:');

const breedCounts = {};
y_train.forEach(label => {
  const breed = idx_to_breed[label];
  if (!breedCounts[breed]) {
    breedCounts[breed] = 0;
  }
  breedCounts[breed]++;
});

Object.entries(breedCounts)
  .sort((a, b) => b[1] - a[1])
  .forEach(([breed, count]) => {
    const percentage = ((count / y_train.length) * 100).toFixed(2);
    console.log(`${breed}: ${count} (${percentage}%)`);
  });
