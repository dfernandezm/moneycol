import { SearchResult, BankNoteInputCollectionItem, StoredBankNoteCollection } from '../src/infrastructure/search/SearchResult';
import { BankNoteCollection } from '../src/infrastructure/search/SearchResult';
import { CollectionResult } from '../src/infrastructure/search/SearchResult';
import { NewCollectionInput } from '../src/infrastructure/search/SearchResult';
import { UpdateCollectionInput } from '../src/infrastructure/search/SearchResult';
import { BankNoteCollectionItem } from '../src/infrastructure/search/SearchResult';
import { BankNote } from "../src/types/BankNote";
import uuid from "uuid/v4";
//This would be a dataSource
//https://www.apollographql.com/docs/tutorial/data-source/

const storedCollections: Array<StoredBankNoteCollection> = [];

const addCollection = (collection: NewCollectionInput): BankNoteCollection => {
    const col = new StoredBankNoteCollection(collection.name, 
        collection.description,
        collection.collectorId);

    col.setCollectionId(uuid());
    storedCollections.push(col);
    return mapToBankNoteCollection(col);
}

const findExistingCollection = (collectionId: string): BankNoteCollection | null => {
    let collections: StoredBankNoteCollection[] = storedCollections.filter(collection => collection.collectionId === collectionId);

    if (collections.length > 0) {
        return new BankNoteCollection(collections[0].collectorId, collections[0].name, 
            collections[0].description, collections[0].collectorId, new Array<BankNote>())
    } else {
        return null;
    }
}

const mapToBankNoteCollection = (collection: StoredBankNoteCollection): BankNoteCollection => {
    return new BankNoteCollection(collection.collectionId,
        collection.name, collection.description, 
        collection.collectorId, mapToBankNotes(collection.bankNotes))
}

const findStoredCollection = (collectionId: string): StoredBankNoteCollection | null => {
    let collections: StoredBankNoteCollection[] = storedCollections.filter(collection => collection.collectionId === collectionId);
    if (collections.length > 0) {
        return collections[0];
    } else {
        return null;
    }
}

const addBankNoteToCollection = (collectionId: string, bankNote: BankNoteCollectionItem): BankNoteCollection => {
    let collection: StoredBankNoteCollection | null = findStoredCollection(collectionId);
    if (collection) {
        collection.addBankNoteItem(bankNote);
        return mapToBankNoteCollection(collection);
    } else {
        throw new Error(`Collection with ID ${collectionId} does not exist`);
    }
} 

const mapToBankNotes = (bankNoteCollectionItems: BankNoteCollectionItem[]): BankNote[] => {

    return bankNoteCollectionItems.map(b => {
        return {
        country: "",
        banknoteName: "",
        year: "",
        catalogCode: b.id,
        description: "",
        detailLink: "",
        imageFront: "",
        imageBack: ""
    }});
}

const byCollector = (collectorId: string): BankNoteCollection[] => {
    console.log("Collections: ", storedCollections);
    let collectionsByCollector = storedCollections
                                    .filter(collection => collection.collectorId === collectorId)
                                    .map(collection => { 
                                        return new BankNoteCollection(
                                            collection.collectionId,
                                            collection.name, 
                                            collection.description, 
                                            collection.collectionId,
                                            mapToBankNotes(collection.bankNotes))});
    console.log("Collections for collector", collectionsByCollector);
    return collectionsByCollector;
}

const deleteCollection = (collectionId: string) => {
    let col = findStoredCollection(collectionId)
    if (col) {
        let index = storedCollections.indexOf(col);
        storedCollections.splice(index, 1)
    }
}

const updateCollection = (collectionId: string, collection: UpdateCollectionInput) => {
    let col = findStoredCollection(collectionId);
    if (col) {
        col.name =collection.name
        col.description = collection.description
        return mapToBankNoteCollection(col);
    }
}

const removeBankNoteFromCollection =  (banknoteId: string, collectionId: string): BankNoteCollection => {
    let col  = findStoredCollection(collectionId);
    if (col) {
        let index = col.bankNotes.findIndex( b => b.id == banknoteId);
        console.log(`Index: ${index}`);
        if (index) {
            col.bankNotes.splice(index, 1);
            return mapToBankNoteCollection(col);
        } else {
            throw Error(`Banknote with id ${banknoteId} does not exist`);
        }

    } else {
        throw Error(`Collection with id ${collectionId} does not exist`);
    }
}

const fakeData = {
    collections: storedCollections,
    addBankNoteToCollection: addBankNoteToCollection,
    findExistingCollection: findExistingCollection,
    addCollection: addCollection,
    byCollector : byCollector,
    updateCollection,
    deleteCollection,
    removeBankNoteFromCollection
}

export default fakeData;