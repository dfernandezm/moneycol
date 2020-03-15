import { RESTDataSource } from 'apollo-datasource-rest';
import { CollectionApiResult } from './types';
import { NewCollectionInput } from '../SearchResult';
import { CollectionCreatedResult } from '../SearchResult';
import {
  Request
} from 'apollo-server-env';
import {
  ApolloError
} from 'apollo-server-errors';

export class CollectionsRestDatasource extends RESTDataSource {
  
  constructor() {
    super();
    this.baseURL = 'http://localhost:8001/';
  }

  async getCollectionsForCollector(collectorId: string): Promise<CollectionApiResult[]> {
    return this.get(`collections/collector/${collectorId}`);
  }

  async getItemsForCollection(collectionId: string): Promise<CollectionApiResult> {
    return this.get(`collections/${collectionId}`);
  }

  async createCollection(collection: NewCollectionInput): Promise<CollectionCreatedResult> {
    return this.post(`collections`, collection);
  }

  //TODO: workaround for now, the server should return this already. Created bug #130 for this
  protected didEncounterError(error: ApolloError, _request: Request) {
    let message = error.extensions.response.body.message;
    console.log("Error from API", message);
    error.extensions.code = "BAD_REQUEST";
    error.extensions.response.status = 400;
    throw error;
  }

};