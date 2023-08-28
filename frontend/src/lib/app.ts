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
export let fullPlaceholderPoll: PollStatus = {
    question: 'placeholder',
    options: [{
        value: "option 1",
        votes: 7,
    }, {
        value: "option 2",
        votes: 3,
    }],
    totalVotes: 10,
    winner: null,
    winnerIndex: null,
    active: true,
    hasVoted: true,
}
export let dialogue: string[][] = [
    ["hey, you", "...?"],
    ["this stream is kinda boring right?", "yeah kinda"],
    ["don't you want to mess with it? 😈", "hell yeah i do"],
    ["i think i just finished hacking in to the stream, i just need you to help me decide what to do", "sounds good"]
]
