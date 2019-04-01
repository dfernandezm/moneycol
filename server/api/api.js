const express = require('express')
const app = express()
const port = 4000

const searcher = require("../elasticSearchImporter")

const bodyParser = require('body-parser'); 
app.use(bodyParser.urlencoded({
    extended: true
}));
app.use(bodyParser.json());

app.get('/search', async (req, res) => {
    const searchQuery = req.query.qs;
    const searchResults =  await searcher.search("es", searchQuery);
    console.log("Results: " + searchResults);        
    const json = { searchResults };
    res.send(json);
});

// error handler
app.use(function (err, req, res, next) {
    console.error(err.stack)
    const errorMessage = { message: err.message};
    res.status(500).send(errorMessage);
  })

app.listen(port, () => console.log(`Example app listening on port ${port}!`))