import { RESTDataSource, RequestOptions } from 'apollo-datasource-rest';
import { CollectionApiResult } from './types';
import { NewCollectionInput } from '../search/SearchResult';
import { CollectionCreatedResult } from '../search/SearchResult';
import { resolverHelper } from '../ResolverHelper';

import {
  Request
} from 'apollo-server-env';
import {
  ApolloError
} from 'apollo-server-errors';
import { tokenHelper } from '../../tokenHelper';

export class CollectionsRestDatasource extends RESTDataSource {
  
  constructor() {
    super();
    this.baseURL = process.env.COLLECTIONS_API_HOST ? 
    `http://${process.env.COLLECTIONS_API_HOST}/` : 
    'http://localhost:8001/';
    console.log("Base url for collections datasource is " + this.baseURL)
  }

  async getCollectionsForCollector(collectorId: string): Promise<CollectionApiResult[]> {
    return this.get(`collections`);
  }

  async getItemsForCollection(collectionId: string): Promise<CollectionApiResult> {
    return this.get(`collections/${collectionId}`);
  }

  async createCollection(collection: NewCollectionInput): Promise<CollectionCreatedResult> {
    return this.post(`collections`, collection);
  }

  async addItemsToCollection(collectionId: string, itemIds: string[]): Promise<void> {
    return this.post(`collections/${collectionId}/items`, { items: itemIds });
  }

  async getCollectionById(collectionId: string): Promise<CollectionApiResult> {
      return this.getItemsForCollection(collectionId);
  }

  async updateCollection(collectionId: string, name: string, description: string): Promise<CollectionCreatedResult> {
    //TODO: the API is not behaving as expected, it should just update these 2 values, but it's also batching the items 
    // so it's deleting them if present as they aren't being sent. Created bug #136 to fix.
    return this.put(`/collections/${collectionId}`, { name: name, description: description });
  }

  async deleteCollection(collectionId: string): Promise<void> {
    return this.delete(`/collections/${collectionId}`);
  }

  async deleteCollectionItem(collectionId: string, itemId: string): Promise<void> {
    return this.delete(`/collections/${collectionId}/items/${itemId}`);
  }

  protected async willSendRequest?(request: RequestOptions) {
     
    // light token decoding, the Collections API validates it
    tokenHelper.validateToken(this.context.token);

    console.log("Setting token in request to collections API: \n", this.context.token);
    request.headers.set("Authorization", "Bearer " + this.context.token);
  }

  protected didEncounterError(error: ApolloError, _request: Request) {
    let message = error.extensions.response.body.message ? error.extensions.response.body.message : error.extensions.response.body.error;
    console.log(`Error from API, code -> ${error.extensions.code}, status -> ${error.extensions.response.status}, message -> ${message}`);
    throw error;
  }

};