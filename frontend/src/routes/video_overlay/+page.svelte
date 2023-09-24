<script lang="ts">
    import Poll from "$lib/Poll.svelte";
    import Dialogue from "$lib/Dialogue.svelte";
    import type {PollState, PollStatus} from "$lib/app";
    import {dialogue, placeholderPoll} from "$lib/app";
    import {onMount} from "svelte";

    let poll: PollStatus = placeholderPoll;
    let dialogue_index = 0;
    function next_dialogue() {
        dialogue_index++;
    }
    let active: boolean;

    onMount(async () => {
        let jsonl = await fetch("/debut.jsonl").then(res => res.text());
        let pollStates = jsonl.split("\n").map(line => {
            let pollState: PollState = JSON.parse(line);
            pollState.status.hasVoted = true;
            return pollState;
        });
        let start = Date.now();
        let dialogueStart = pollStates[0].time;
        let currentPollTime = 0;
        setInterval(async () => {
            let time = Math.floor((Date.now() - start) / 1000);

            if (dialogue_index < dialogue.length && (time - dialogueStart) >= 5) {
                next_dialogue();
                dialogueStart += 5;
            }

            for (let pollState of pollStates) {
                if (time >= pollState.time && currentPollTime < pollState.time) {
                    poll = pollState.status;
                    active = poll.active;
                }
            }
        }, 1000);
    });
</script>

<div id="transition" class="w-1/3 max-w-xs mx-auto fixed right-6 top-6 pointer-events-none" style="opacity: {active ? '1' : '0'}; transform: {active ? 'translateY(0)' : 'translateY(-100%)'}">
    {#if dialogue_index < dialogue.length}
        <Dialogue index={dialogue_index} active={active} next={next_dialogue} />
    {:else}
        <Poll {poll} authToken={undefined} />
    {/if}
</div>

<style>
    #transition {
        transition-property: opacity, transform;
        transition-duration: 2s;
        transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
    }
</style>
