import mongoose from 'mongoose';

export const connectMongoDB = async () => {
  await mongoose.connect('mongodb://localhost:27017/nowpaidb', { useNewUrlParser: true , useUnifiedTopology: true});
}