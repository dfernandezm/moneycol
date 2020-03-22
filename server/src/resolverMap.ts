//Consider: https://typegraphql.ml/
//https://www.compose.com/articles/use-all-the-databases-part-2/#elasticsearch

import { IResolvers, addSchemaLevelResolveFunction } from 'graphql-tools';
import { SearchService } from './infrastructure/SearchService';
import { ElasticSearchService } from './infrastructure/ElasticSearchService';
import { SearchResult, CollectionResult } from './infrastructure/SearchResult';
import { BankNoteCollection } from './infrastructure/SearchResult';
import { NewCollectionInput } from './infrastructure/SearchResult';
import { AddBankNoteToCollection } from './infrastructure/SearchResult';
import { UpdateCollectionInput } from './infrastructure/SearchResult';
import { BankNote } from './types/BankNote';
import fakeData from './fakeData';
import decorator from './decorator';
import { CollectionApiResult } from "./infrastructure/collections/types";

const searchService: SearchService = new ElasticSearchService();



const resolverMap: IResolvers = {
    Query: {
        async search(_: void, args: { term: string, from: number, to: number }, ctx): Promise<SearchResult> {
            return searchService.search("en", args.term, args.from, args.to);
        },
        async collections(_: void, args: { collectorId: string }, { dataSources }): Promise<BankNoteCollection[]> {
            let collections: CollectionApiResult[] = await dataSources.collectionsAPI.getCollectionsForCollector(args.collectorId);
            // These collections won't require the items for now, so we send it empty for now
            return collections.map(col => new BankNoteCollection(col.id, col.name, col.description, col.collectorId, []));
        },
        async itemsForCollection(_: void, args: { collectionId: string }, { dataSources }): Promise<BankNoteCollection> {
            let collection: CollectionApiResult = await dataSources.collectionsAPI.getItemsForCollection(args.collectionId);
            console.log("Items in collection:", collection.items);
            let bankNotes: BankNote[] = await decorator.decorateItems("en", collection.items);
            console.log("Decorated banknotes:", bankNotes);
            return new BankNoteCollection(collection.id, collection.name, collection.description, collection.collectorId, bankNotes);
        }
    },
    Mutation: {

        async addCollection(_: void, args: { collection: NewCollectionInput }, { dataSources }): Promise<BankNoteCollection | null> {
            let { collection } = args
            console.log(`About to create collection for ${collection.collectorId}: ${collection.name}, ${collection.description}`);
            let { collectionId, name, description, collectorId } = await dataSources.collectionsAPI.createCollection(collection);
            return new BankNoteCollection(collectionId, name, description, collectorId, []);
        },

        async addBankNoteToCollection(_: void, args: { data: AddBankNoteToCollection }, { dataSources }): Promise<BankNoteCollection> {
            let { data: { collectionId, collectorId, bankNoteCollectionItem: { id } } } = args;
            console.log(`Adding banknote to collection: ${collectionId}`);

            await dataSources.collectionsAPI.addItemsToCollection(collectionId, [id]);
            
            //TODO: the API should return the collection back (1st page or so): issue #133
            let fetchedCollection: CollectionApiResult = await dataSources.collectionsAPI.getCollectionById(collectionId);
            let bankNotes: BankNote[] = await decorator.decorateItems("en", fetchedCollection.items);
            return new BankNoteCollection(collectionId, fetchedCollection.name, 
                                        fetchedCollection.description, collectorId, bankNotes);
        },

        async updateCollection(_: void, args: { collectionId: string, data: UpdateCollectionInput }, { dataSources }): Promise<BankNoteCollection> {
            let { name, description } = args.data;
            let bankNoteCollection = await dataSources.collectionsAPI.updateCollection(args.collectionId, name, description);
            return new BankNoteCollection(
                bankNoteCollection.collectionId, 
                bankNoteCollection.name,
                bankNoteCollection.description,
                bankNoteCollection.collectorId, []);
        },

        async deleteCollection(_: void, args: { collectionId: string }, { dataSources }): Promise<Boolean> {
            console.log(`Deleting collection ${args.collectionId}`);
            await dataSources.collectionsAPI.deleteCollection(args.collectionId);
            return true;
        },

        async removeBankNoteFromCollection(_: void, args: { banknoteId: string, collectionId: string }): Promise<BankNoteCollection> {
            let bankNoteCollection = fakeData.removeBankNoteFromCollection(args.banknoteId, args.collectionId);
            return bankNoteCollection;
        }
    }
};

export default resolverMap;