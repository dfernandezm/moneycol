import { RESTDataSource } from 'apollo-datasource-rest';

//TODO: investigate how it would map to endpoints
export class CollectionsRestDatasource extends RESTDataSource {
  constructor() {
    super();
    this.baseURL = 'https://mvrp.herokuapp.com/api/';
  }

  async getAllCars() {
    return this.get('cars');
  }

  async getACar(plateNumber) {
    const result = await this.get('car', {
      plateNumber
    });

    return result[0];
  }
};