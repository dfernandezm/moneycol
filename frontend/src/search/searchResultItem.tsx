import React from 'react';
import './css/searchResult.css';
import { SearchResult } from './types/SearchResult';

type SearchResultsItemProps = {
    item: SearchResult,
    index: number
}

const SearchResultsItem: React.FC<SearchResultsItemProps> = ({ item, index }) => {
    return (
        <div className="card horizontal" key={item.CatalogCode}>
            <div className="card-image">
                <div className="image-wrapper">
                    <img src={item.ImageFront} alt="img" />
                </div>
            </div>
            <div className="card-stacked">
                <div className="card-content">
                    <h3>{item.Country} ==== {item.BanknoteName}</h3>
                    <h4>{item.Year}</h4>
                    <p>{item.Description}</p>
                    <p><a href={item.DetailLink} target="_blank">Link</a></p>
                </div>
                <div className="card-action">
                    <a href="#" className="green-link">Add to collection</a>
                </div>
            </div>
        </div>
    )
}

export default SearchResultsItem;

