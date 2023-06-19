declare global {
    namespace App {
        // interface Error {}
        // interface Locals {}
        // interface PageData {}
        // interface Platform {}
    }
}

export {};

export interface Option {
    value: string;
    votes: number;
}

export interface PollStatus {
    question: string;
    options: Option[];
    totalVotes: number;
    winner: string | null;
    active: boolean;
    hasVoted: boolean;
}

export interface ErrorResult {
    error: string;
}
