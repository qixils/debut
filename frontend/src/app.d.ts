declare global {
    namespace App {
        // interface Error {}
        // interface Locals {}
        // interface PageData {}
        // interface Platform {}
    }
}

export {};

export class Option {
    value: string;
    votes: number;
}

export class PollStatus {
    question: string;
    options: Option[];
    totalVotes: number;
    winner: string | null;
    active: boolean;
    hasVoted: boolean;
}
