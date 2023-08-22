export interface Option {
    value: string;
    votes: number;
}

export interface PollStatus {
    question: string;
    options: Option[];
    totalVotes: number;
    winner: string | null;
    winnerIndex: number | null;
    active: boolean;
    hasVoted: boolean;
}

export interface ErrorResult {
    error: string;
}

export class PollPreset {
    constructor(public question: string, public options: string[]) {}
}

export let placeholderPoll: PollStatus = {
    question: 'Loading...',
    options: [],
    totalVotes: 0,
    winner: null,
    winnerIndex: null,
    active: false,
    hasVoted: true,
}
