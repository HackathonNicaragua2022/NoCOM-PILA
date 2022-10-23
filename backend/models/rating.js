import { Schema } from 'mongoose';

const Rating = new Schema({
    user: { type: Schema.Types.ObjectId },
    name: String,
    text: String,
    stars: Number
})

export {
    Rating
}