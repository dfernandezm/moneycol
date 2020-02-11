//Consider: https://typegraphql.ml/
//https://www.compose.com/articles/use-all-the-databases-part-2/#elasticsearch

import { IResolvers, addSchemaLevelResolveFunction } from 'graphql-tools';
import { SearchService } from './infrastructure/SearchService';
import { ElasticSearchService } from './infrastructure/ElasticSearchService';
import { SearchResult } from './infrastructure/SearchResult';
import { BankNoteCollection } from './infrastructure/SearchResult';
import { CollectionResult } from './infrastructure/SearchResult';
import { NewCollectionInput } from './infrastructure/SearchResult';
import { AddBankNoteToCollection } from './infrastructure/SearchResult';
import { UpdateCollectionInput } from './infrastructure/SearchResult';


import fakeData from './fakeData';
import { BankNote } from './types/BankNote';

const searchService: SearchService = new ElasticSearchService();

const resolverMap: IResolvers = {
    Query: {
        async search(_: void, args: { term: string, from: number, to: number }, ctx): Promise<SearchResult> {
            return searchService.search("en", args.term, args.from, args.to);
        },
        async collections(_: void, args: {collectorId: string}): Promise<BankNoteCollection[]> {
            let collectionsForCollector = fakeData.byCollector(args.collectorId);
            return Promise.resolve(collectionsForCollector);
        }
    },
    Mutation: {
        async addCollection(_: void, args: {collection: NewCollectionInput}): Promise<BankNoteCollection | null>  {
            console.log(`About to create collection for ${args.collection.collectorId}: ${args.collection.name}, ${args.collection.description}`)
            let bankNoteCollection = fakeData.addCollection(args.collection);
            return Promise.resolve(bankNoteCollection);
        },
        async addBankNoteToCollection(_: void, args: { data: AddBankNoteToCollection }): Promise<BankNoteCollection> {
            console.log(`Adding banknote to collection: ${args.data.collectionId}`)
            let bankNoteCollection: BankNoteCollection = fakeData.addBankNoteToCollection(args.data.collectionId, args.data.bankNoteCollectionItem);
            if (bankNoteCollection) {
                console.log(`Returning banknote collection:`, bankNoteCollection);
                return bankNoteCollection;
            } else {
                console.log(`Collection with ${args.data.collectionId} not found`);
                return Promise.reject({error: `Collection with ${args.data.collectionId} not found`});
            }
        },
        async updateCollection(_: void, args: {collectionId: string, data: UpdateCollectionInput}): Promise<BankNoteCollection> {
            let bankNoteCollection = fakeData.updateCollection(args.collectionId, args.data)
            if (bankNoteCollection) {
                return Promise.resolve(bankNoteCollection)
            } else {
                return Promise.reject(`Cannot update collection ${args.collectionId}, ${bankNoteCollection}`)
            }
        },
        async deleteCollection(_: void, args: {collectionId: string}): Promise<Boolean> {
            console.log(`Deleting collection ${args.collectionId}`);

            
            fakeData.deleteCollection(args.collectionId)
            return Promise.resolve(true);
        },
        async removeBankNoteFromCollection(_: void, args: {banknoteId: string, collectionId: string}): Promise<BankNoteCollection> {
            let bankNoteCollection = fakeData.removeBankNoteFromCollection(args.banknoteId, args.collectionId);
            return bankNoteCollection;
        }
    }
};

export default resolverMap;