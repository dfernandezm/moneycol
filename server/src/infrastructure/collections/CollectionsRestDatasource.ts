import { RESTDataSource } from 'apollo-datasource-rest';
import { CollectionApiResult } from './types';

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
};