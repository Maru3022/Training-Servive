import React, { useState, useMemo, useCallback, memo } from 'react';
import TrainingService from './services/TrainingService';
import type { ITraining } from './types';
import { Dumbbell, Play, Database, Flame, Clock, Zap, Target, ChevronRight, TrendingUp, Star, BarChart2 } from 'lucide-react';

interface IRecommendation {
    id: number;
    name: string;
    category: 'Силовая' | 'Кардио' | 'Растяжка' | 'HIIT';
    duration: number;
    difficulty: 1 | 2 | 3;
    muscles: string[];
    calories: number;
    trending?: boolean;
    featured?: boolean;
    color: string;
}

const RECOMMENDATIONS: IRecommendation[] = [
    { id: 1, name: 'Утренний разгон', category: 'HIIT', duration: 20, difficulty: 2, muscles: ['Всё тело'], calories: 280, featured: true, trending: true, color: 'orange' },
    { id: 2, name: 'Жим лёжа + суперсет', category: 'Силовая', duration: 45, difficulty: 3, muscles: ['Грудь', 'Трицепс'], calories: 320, color: 'blue' },
    { id: 3, name: 'Восстановительный бег', category: 'Кардио', duration: 30, difficulty: 1, muscles: ['Ноги', 'Сердце'], calories: 210, color: 'emerald' },
    { id: 4, name: 'Спина и бицепс', category: 'Силовая', duration: 50, difficulty: 3, muscles: ['Спина', 'Бицепс'], calories: 340, color: 'blue' },
    { id: 5, name: 'Вечерняя растяжка', category: 'Растяжка', duration: 15, difficulty: 1, muscles: ['Всё тело'], calories: 60, color: 'violet' },
    { id: 6, name: 'Табата-взрыв', category: 'HIIT', duration: 25, difficulty: 3, muscles: ['Кор', 'Ноги'], calories: 350, trending: true, color: 'orange' },
    { id: 7, name: 'Плечи и пресс', category: 'Силовая', duration: 40, difficulty: 2, muscles: ['Плечи', 'Кор'], calories: 290, color: 'blue' },
    { id: 8, name: 'Велотрек', category: 'Кардио', duration: 35, difficulty: 2, muscles: ['Ноги'], calories: 260, color: 'emerald' },
];

const CATEGORIES = ['Все', 'Силовая', 'Кардио', 'HIIT', 'Растяжка'] as const;
type TFilter = typeof CATEGORIES[number];

const DIFFICULTY_LABELS: Record<number, string> = { 1: 'Легко', 2: 'Средне', 3: 'Тяжело' };

const COLOR_MAP: Record<string, { bg: string; border: string; text: string; badge: string; dot: string; btnBg: string; btnHover: string }> = {
    orange: { bg: 'bg-orange-500/10', border: 'border-orange-500/30', text: 'text-orange-400', badge: 'bg-orange-500/20 text-orange-300', dot: 'bg-orange-500', btnBg: 'bg-orange-500', btnHover: 'hover:bg-orange-400' },
    blue:   { bg: 'bg-blue-500/10',   border: 'border-blue-500/30',   text: 'text-blue-400',   badge: 'bg-blue-500/20 text-blue-300',   dot: 'bg-blue-500',   btnBg: 'bg-blue-600', btnHover: 'hover:bg-blue-500' },
    emerald:{ bg: 'bg-emerald-500/10', border: 'border-emerald-500/30',text: 'text-emerald-400',badge: 'bg-emerald-500/20 text-emerald-300',dot: 'bg-emerald-500', btnBg: 'bg-emerald-600', btnHover: 'hover:bg-emerald-500'},
    violet: { bg: 'bg-violet-500/10',  border: 'border-violet-500/30', text: 'text-violet-400', badge: 'bg-violet-500/20 text-violet-300', dot: 'bg-violet-500', btnBg: 'bg-violet-600', btnHover: 'hover:bg-violet-500' },
};

interface IDifficultyDotsProps {
    level: 1 | 2 | 3;
    color: string;
}

const DifficultyDots = memo<IDifficultyDotsProps>(({ level, color }) => (
    <div className="flex gap-1">
        {[1, 2, 3].map(i => (
            <span key={i} className={`w-2 h-2 rounded-full ${i <= level ? COLOR_MAP[color].dot : 'bg-slate-700'}`} />
        ))}
    </div>
));

DifficultyDots.displayName = 'DifficultyDots';

interface IFeaturedCardProps {
    rec: IRecommendation;
    onStart: (rec: IRecommendation) => void;
}

const FeaturedCard = memo<IFeaturedCardProps>(({ rec, onStart }) => {
    const c = COLOR_MAP[rec.color];
    const handleClick = useCallback(() => onStart(rec), [rec, onStart]);

    return (
        <div className={`relative rounded-2xl border ${c.border} ${c.bg} p-6 overflow-hidden col-span-2`}>
            <div className={`absolute -right-10 -top-10 w-48 h-48 rounded-full opacity-10 ${c.dot}`} />
            <div className={`absolute -right-5 -top-5 w-28 h-28 rounded-full opacity-10 ${c.dot}`} />
            <div className="flex items-start justify-between relative z-10">
                <div>
                    <div className="flex items-center gap-2 mb-3">
                        <span className={`text-xs font-bold px-2 py-0.5 rounded-full ${c.badge} uppercase tracking-widest`}>⚡ Рекомендуется сегодня</span>
                        {rec.trending && <span className="text-xs font-bold px-2 py-0.5 rounded-full bg-rose-500/20 text-rose-300 uppercase tracking-widest">🔥 Популярно</span>}
                    </div>
                    <h2 className="text-3xl font-black tracking-tight text-white mb-1">{rec.name}</h2>
                    <p className={`text-sm font-semibold ${c.text} mb-4`}>{rec.category}</p>
                    <div className="flex gap-5 text-sm text-slate-400 mb-6">
                        <span className="flex items-center gap-1.5"><Clock size={14} /> {rec.duration} мин</span>
                        <span className="flex items-center gap-1.5"><Flame size={14} /> {rec.calories} ккал</span>
                        <span className="flex items-center gap-1.5"><Target size={14} /> {rec.muscles.join(', ')}</span>
                    </div>
                    <button onClick={handleClick} className={`flex items-center gap-2 px-5 py-2.5 rounded-xl font-bold text-sm transition-all hover:scale-105 active:scale-95 text-white ${c.btnBg} ${c.btnHover}`}>
                        <Play size={16} fill="white" /> Начать тренировку
                    </button>
                </div>
                <div className="hidden sm:flex flex-col items-end gap-3">
                    <div className="text-right">
                        <p className="text-xs text-slate-500 uppercase tracking-wider mb-1">Сложность</p>
                        <DifficultyDots level={rec.difficulty} color={rec.color} />
                        <p className={`text-xs font-bold mt-1 ${c.text}`}>{DIFFICULTY_LABELS[rec.difficulty]}</p>
                    </div>
                </div>
            </div>
        </div>
    );
});

FeaturedCard.displayName = 'FeaturedCard';

interface ITrainingCardProps {
    rec: IRecommendation;
    onStart: (rec: IRecommendation) => void;
    isLoading?: boolean;
}

const TrainingCard = memo<ITrainingCardProps>(({ rec, onStart, isLoading }) => {
    const c = COLOR_MAP[rec.color];
    const handleClick = useCallback(() => onStart(rec), [rec, onStart]);

    return (
        <div className={`group relative rounded-2xl border ${c.border} bg-slate-900/60 p-5 flex flex-col gap-4 cursor-pointer ${c.bg} transition-all duration-300`}
             onClick={handleClick}>
            {isLoading && (
                <div className="absolute inset-0 z-10 rounded-2xl bg-slate-950/70 flex items-center justify-center">
                    <div className="w-5 h-5 border-2 border-orange-500 border-t-transparent rounded-full animate-spin" />
                </div>
            )}
            <div className="flex items-center justify-between">
                <span className={`text-xs font-bold px-2 py-0.5 rounded-full ${c.badge} uppercase tracking-widest`}>{rec.category}</span>
                {rec.trending ? <TrendingUp size={14} className="text-rose-400" /> : <Star size={14} className="text-slate-600 group-hover:text-slate-400 transition" />}
            </div>
            <div>
                <h3 className="text-lg font-black text-white tracking-tight">{rec.name}</h3>
                <p className="text-xs text-slate-500 mt-0.5">{rec.muscles.join(' · ')}</p>
            </div>
            <div className="flex items-center gap-4 text-xs text-slate-400">
                <span className="flex items-center gap-1"><Clock size={12} /> {rec.duration} мин</span>
                <span className="flex items-center gap-1"><Flame size={12} /> {rec.calories} ккал</span>
            </div>
            <div className="flex items-center justify-between mt-auto">
                <DifficultyDots level={rec.difficulty} color={rec.color} />
                <ChevronRight size={16} className={`${c.text} opacity-0 group-hover:opacity-100 transition`} />
            </div>
        </div>
    );
});

TrainingCard.displayName = 'TrainingCard';

const App: React.FC = () => {
    const [message, setMessage] = useState<string>('');
    const [messageType, setMessageType] = useState<'success' | 'error'>('success');
    const [activeFilter, setActiveFilter] = useState<TFilter>('Все');
    const [loadingId, setLoadingId] = useState<number | null>(null);

    const handleBulkLoad = useCallback(async () => {
        try {
            const response = await TrainingService.bulkUpload(1000000);
            setMessage(response);
            setMessageType('success');
        } catch {
            setMessage('Ошибка при связи с сервером');
            setMessageType('error');
        }
    }, []);

    const handleStartTraining = useCallback(async (rec: IRecommendation) => {
        setLoadingId(rec.id);
        const newTraining: ITraining = { training_name: rec.name, status: 'PLANNED' };
        try {
            const requestId = await TrainingService.createTraining(newTraining);
            setMessage(`Запланировано: «${rec.name}» — ID: ${requestId}`);
            setMessageType('success');
        } catch {
            setMessage('Ошибка: проверьте, запущен ли Backend');
            setMessageType('error');
        } finally {
            setLoadingId(null);
        }
    }, []);

    const featured = useMemo(() => RECOMMENDATIONS.find(r => r.featured)!, []);
    const filtered = useMemo(() => RECOMMENDATIONS.filter(r => !r.featured && (activeFilter === 'Все' || r.category === activeFilter)), [activeFilter]);
    const weekData = useMemo(() => [40, 70, 55, 90, 30, 80, 60], []);

    const handleCloseMessage = useCallback(() => setMessage(''), []);

    return (
        <div className="min-h-screen bg-slate-950 text-white font-sans">
            <header className="sticky top-0 z-30 bg-slate-950/80 backdrop-blur-md border-b border-slate-800/60 px-6 py-4">
                <div className="max-w-5xl mx-auto flex items-center justify-between">
                    <h1 className="text-2xl font-black tracking-tight flex items-center gap-2 italic">
                        <Dumbbell size={22} className="text-orange-500" />
                        POWER<span className="text-orange-500">LOG</span>
                    </h1>
                    <div className="flex items-center gap-3">
                        <button onClick={handleBulkLoad} title="Bulk load" className="p-2 rounded-lg bg-slate-800 hover:bg-slate-700 transition text-slate-400 hover:text-white">
                            <Database size={18} />
                        </button>
                        <div className="flex items-center gap-1.5 text-sm text-slate-400">
                            <BarChart2 size={16} className="text-orange-500" />
                            <span className="font-bold text-white">{RECOMMENDATIONS.length}</span> тренировок
                        </div>
                    </div>
                </div>
            </header>

            <main className="max-w-5xl mx-auto px-6 py-8">
                {message && (
                    <div className={`mb-6 flex items-center gap-3 px-4 py-3 rounded-xl border text-sm font-medium ${messageType === 'success' ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-300' : 'bg-rose-500/10 border-rose-500/30 text-rose-300'}`}>
                        <Zap size={16} />
                        {message}
                        <button onClick={handleCloseMessage} className="ml-auto opacity-50 hover:opacity-100 transition text-lg leading-none">×</button>
                    </div>
                )}

                <div className="mb-6">
                    <p className="text-xs text-slate-500 uppercase tracking-widest font-bold mb-1">Лента рекомендаций</p>
                    <h2 className="text-2xl font-black tracking-tight">Что тренируем сегодня?</h2>
                </div>

                <div className="grid grid-cols-2 gap-4 mb-4">
                    <FeaturedCard rec={featured} onStart={handleStartTraining} />
                    <div className="flex flex-col gap-4">
                        <div className="rounded-2xl border border-slate-800 bg-slate-900/60 p-5 flex flex-col justify-between h-full">
                            <p className="text-xs text-slate-500 uppercase tracking-widest font-bold mb-2">Твоя неделя</p>
                            <div className="flex gap-2 items-end h-16">
                                {weekData.map((h, i) => (
                                    <div key={i} className="flex-1 flex flex-col justify-end">
                                        <div className={`rounded-sm ${i === 4 ? 'bg-orange-500' : 'bg-slate-700'}`} style={{ height: `${h}%` }} />
                                    </div>
                                ))}
                            </div>
                            <div className="flex justify-between text-xs text-slate-600 mt-1">
                                {['Пн','Вт','Ср','Чт','Пт','Сб','Вс'].map(d => <span key={d}>{d}</span>)}
                            </div>
                        </div>
                    </div>
                </div>

                <div className="flex gap-2 mb-6 flex-wrap">
                    {CATEGORIES.map(cat => (
                        <button key={cat} onClick={() => setActiveFilter(cat)}
                                className={`px-4 py-1.5 rounded-full text-xs font-bold uppercase tracking-wider transition-all ${activeFilter === cat ? 'bg-orange-500 text-white' : 'bg-slate-800 text-slate-400 hover:text-white hover:bg-slate-700'}`}>
                            {cat}
                        </button>
                    ))}
                </div>

                <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
                    {filtered.map(rec => (
                        <TrainingCard key={rec.id} rec={rec} onStart={handleStartTraining} isLoading={loadingId === rec.id} />
                    ))}
                </div>
            </main>
        </div>
    );
};

export default App;