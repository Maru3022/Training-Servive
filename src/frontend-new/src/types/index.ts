export type UUID = string;

export interface ITraining {
    id?: UUID;
    data?: string; // ISO date YYYY-MM-DD
    userId?: UUID; // UUID as string or will be auto-generated
    training_name: string;
    status: 'PLANNED' | 'IN_PROGRESS' | 'COMPLETED';
    exercises?: IExercise[];
}

export interface IExercise {
    id?: UUID;
    trainingId?: UUID;
    name_exercise: string;
    notes?: string;
    sets?: IExerciseSet[];
}

export interface IExerciseSet {
    id?: UUID;
    exerciseId?: UUID;
    weight: number;
    reps: number;
    order: number;
}