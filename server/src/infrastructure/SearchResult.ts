import { BankNote } from "../types/BankNote";

export class SearchResult {
    constructor(public results: BankNote[], public total: number) {}
}