import { Schema } from 'mongoose';

const Product =  new Schema({
    name: String,
    money: String,
    image: String,
    price: Number
});

export {
    Product
}