const mongoose = require('mongoose')
const barcodeschema = new mongoose.Schema({

})
const barcodedata = mongoose.model('barcodedata',barcodeschema)
module.exports = barcodedata
