const express = require('express');
var proxy = require('express-http-proxy');

const path = require('path');
const app = express();

// This in injected into the container from the deployment.yaml (chart)
const API_BASE_URL = process.env.API_BASE_URL || "localhost:4000"

app.use(express.static(path.join(__dirname, '../build')));

app.get('/graphql', proxy(`${API_BASE_URL}/graphql`)); // Browser makes a GET
app.post('/graphql', proxy(`${API_BASE_URL}/graphql`)); // Apollo client makes a POST for this

app.get('/*', function (req, res) {
    res.sendFile(path.join(__dirname, '../build', 'index.html'));
});

app.listen(5080, () => {
    console.log("React App Server running on port 5080");
    console.log(`GraphQL server URL: http://${API_BASE_URL}/graphql`);
});