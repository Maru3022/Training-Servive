import React, { useState } from 'react';
import TrainingService from './services/TrainingService';
import { ITraining } from './types';
import { Dumbbell, Play, Database, AlertCircle } from 'lucide-react';

const App: React.FC = () => {
    const [message, setMessage] = useState<string>('');

    const handleBulkLoad = async () => {
        try {
            const response = await TrainingService.bulkUpload(1000000);
            setMessage(response);
        } catch (error) {
            setMessage("Ошибка при связи с сервером");
        }
    };

    const createTraining = async () => {
        const newTraining: ITraining = {
            training_name: "Morning Workout",
            status: 'PLANNED'
        };

        try {
            const requestId = await TrainingService.createTraining(newTraining);
            setMessage(`Запрос отправлен! UUID запроса: ${requestId}`);
        } catch (error) {
            setMessage("Ошибка: проверьте, запущен ли Backend на 8085");
        }
    };

    return (
        <div className="min-h-screen bg-slate-950 text-white p-10 font-sans">
            <header className="flex justify-between items-center mb-10 border-b border-slate-800 pb-5">
                <h1 className="text-3xl font-bold flex items-center gap-2 italic">
                    <Dumbbell className="text-blue-500" /> POWER<span className="text-blue-500">LOG</span>
                </h1>
                <div className="flex gap-4">
                    <button onClick={handleBulkLoad} className="bg-slate-800 p-2 rounded-lg hover:bg-slate-700 transition">
                        <Database size={20} />
                    </button>
                    <button onClick={createTraining} className="bg-blue-600 px-6 py-2 rounded-lg font-bold hover:bg-blue-500 transition flex items-center gap-2">
                        <Play size={18} /> Начать сессию
                    </button>
                </div>
            </header>

            {message && (
                <div className="bg-blue-900/20 border border-blue-500/50 p-4 rounded-xl flex items-center gap-3">
                    <AlertCircle size={20} className="text-blue-400" />
                    <span className="text-blue-100">{message}</span>
                </div>
            )}

            <div className="mt-20 text-center opacity-20">
                <Dumbbell size={120} className="mx-auto mb-4" />
                <p>Готов к работе с Kafka и PostgreSQL</p>
            </div>
        </div>
    );
};

export default App;