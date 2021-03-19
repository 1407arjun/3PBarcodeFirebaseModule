const mongoose = require('mongoose')
mongoose.connect('mongodb://localhost/barcode_item_list')
const db = mongoose.connection;
db.on('error',console.log.bind(console,'error in connecting to db'))
db.once('open',function(){
    console.log('Successfully connected to the db')
})
