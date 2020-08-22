import { BankNote } from "../../types/BankNote";

export class SearchResult {
    constructor(public results: BankNote[], public total: number) {}
}

export class BankNoteCollection {
    constructor(public collectionId: string, public name: string, 
        public description: string, 
        public collectorId: string, 
        public bankNotes: BankNote[]) {}
}

export type BankNoteCollectionItem = {
    id: string
}

export class StoredBankNoteCollection {
    public bankNotes: BankNoteCollectionItem[] = new Array<BankNoteCollectionItem>();
    public collectionId: string = "";

    constructor(public name: string, public description: string, public collectorId: string) {}

    public addBankNoteItem(bankNoteItem: BankNoteCollectionItem) {
        this.bankNotes.push(bankNoteItem);
    }

    public setCollectionId(collectionId: string) {
        this.collectionId = collectionId;
    }
}

export type UpdateCollectionInput = {
    name: string,
    description: string
}

export type BankNoteInputCollectionItem = {   
    id: string
}

export type AddBankNoteToCollection = {
    collectionId: string,
    collectorId: string,
    bankNoteCollectionItem: BankNoteCollectionItem
}

export class CollectionResult {
    constructor(public collections: BankNoteCollection[]) {}
}

export type NewCollectionInput = {
    name: string,
    description: string,
    collectorId: string
}

export type CollectionCreatedResult = {
    collectionId: string, //TODO: inconsistent with the above in the API, raised #127 to fix
    name: string,
    description: string,
    collectorId: string
}