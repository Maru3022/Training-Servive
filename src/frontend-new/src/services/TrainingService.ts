import { apiClient } from '../api/apiClient';
import type { ITraining } from '../types';

class TrainingService {
    private readonly endpoint = '/trainings';

    async getTrainings(): Promise<ITraining[]> {
        const { data } = await apiClient.get<ITraining[]>(this.endpoint);
        return data;
    }

    async createTraining(trainingData: ITraining): Promise<string> {
        const { data } = await apiClient.post<string>(this.endpoint, trainingData);
        return data;
    }

    async bulkUpload(count: number = 1000000): Promise<string> {
        const { data } = await apiClient.post<string>(`${this.endpoint}/bulk-load`, null, {
            params: { count }
        });
        return data;
    }
}

export default new TrainingService();