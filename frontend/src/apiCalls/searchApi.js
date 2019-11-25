const API = `http://localhost:4000/search?qs=%query%&from=%from%&size=%size%`;
const data = {
    total: 1,
    results: [{
        CatalogCode: "wc-11a",
        ImageFront: "http://www.google.com",
        Country: "USA",
        BanknoteName: "10 Dollars",
        Year: "1988",
        Description: "Good condition",
        DetailLink: "http://detail.com"
    }]
}
//TODO: sanitize search term before sending (lowercase etc)
const searchApiCall = (searchTerm, from, size) => {
    console.log("Making call...", searchTerm, from, size);
    let apiReplaced = API.replace("%query%", searchTerm);
    apiReplaced = apiReplaced.replace("%from%", from);
    apiReplaced = apiReplaced.replace("%size%", size);
    return Promise.resolve(data);
    //     return fetch(apiReplaced)
    //               .then(response => response.json())
    //               .then(data => data.searchResults);
    // };
}

export default {
    searchApiCall
};