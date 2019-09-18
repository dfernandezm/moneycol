import { SearchResult } from "./SearchResult";

interface SearchService {
      search(language: string, searchTerm: string, from: number, size: number): Promise<SearchResult>;
}

export { SearchService };