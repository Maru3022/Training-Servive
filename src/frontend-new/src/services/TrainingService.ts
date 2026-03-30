import { apiClient } from '../api/apiClient';
import type { ITraining } from '../types';

class TrainingService {
    private readonly endpoint = '/trainings';

    async createTraining(trainingData: ITraining): Promise<string> {
        // Add required fields if not provided
        const payload: ITraining = {
            ...trainingData,
            data: trainingData.data || new Date().toISOString().split('T')[0], // Current date in YYYY-MM-DD
            userId: trainingData.userId || '00000000-0000-0000-0000-000000000000', // Default UUID
        };
        const { data } = await apiClient.post<string>(this.endpoint, payload);
        return data;
    }

    async bulkUpload(count: number = 1000000): Promise<string> {
        const { data } = await apiClient.post<string>(`${this.endpoint}/bulk-load`, null, {
            params: { count }
        });
        return data;
    }

    async getTrainingById(id: string): Promise<ITraining> {
        const { data } = await apiClient.get<ITraining>(`${this.endpoint}/${id}`);
        return data;
    }
}

export default new TrainingService();