import React from 'react';
import './css/searchResult.css';
import { SearchResult } from './types/SearchResult';

type SearchResultsItemProps = {
    item: SearchResult,
    index: number
}

const SearchResultsItem: React.FC<SearchResultsItemProps> = ({ item, index }) => {
    console.log("Item: ", item);
    return (
        <div className="card horizontal" key={item.catalogCode}>
            <div className="card-image">
                <div className="image-wrapper">
                    <img src={item.imageFront} alt="img" />
                </div>
            </div>
            <div className="card-stacked">
                <div className="card-content">
                    <h3>{item.country} - {item.banknoteName}</h3>
                    <h4>{item.year}</h4>
                    <p>{item.description}</p>
                    <p><a href={item.detailLink} target="_blank">Detail</a></p>
                </div>
                <div className="card-action">
                    <a href="#" className="green-link">Add to collection</a>
                </div>
            </div>
        </div>
    )
}

export default SearchResultsItem;

