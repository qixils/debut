<script lang="ts">
    import Poll from "$lib/Poll.svelte";
    import type {PollStatus} from "src/app.js";
    import {PollPreset} from "src/app.js";
    import {onMount} from "svelte";

    let currentPreset: PollPreset | undefined;
    let currentPoll: PollStatus | undefined;
    let previousPoll: PollStatus | undefined;
    let presets: PollPreset[] = [
        new PollPreset("Hi", ["Hello", "Hi", "Hey"]),
    ];

    onMount(async () => setInterval(() => {
        if (currentPoll === undefined) {
            return; // TODO: remove this if i ever publicly release this
        }
        // TODO: currentPoll = <web request>
    }, 1000));

    function closePoll() {
        // TODO: previousPoll = <web request>
        previousPoll.hasVoted = true; // disable voting
        // remove winning option from preset
        if (currentPreset !== undefined) {
            currentPreset.options = currentPreset.options.filter(option => option !== previousPoll.winner);
        }
    }

    function runPoll(question: string, options: string[]) {
        currentPreset = undefined;
        // TODO: currentPoll = <web request>
    }

    function runPreset(preset: PollPreset) {
        currentPreset = preset;
        // TODO: currentPoll = <web request>
    }
</script>

<main>
    <div>
        <h2>Active Poll</h2>
        {#if currentPoll !== undefined}
            <Poll poll={currentPoll} />
            <button on:click={closePoll}>Close Poll</button>
        {:else}
            <p>No active poll</p>
        {/if}
    </div>

    <div>
        <h2>Previous Poll</h2>
        {#if previousPoll !== undefined}
            <Poll poll={previousPoll} />
            <p>Winner: {previousPoll.winner}</p>
        {:else}
            <p>No previous poll</p>
        {/if}
    </div>

    <div>
        <h2>Presets</h2>
        {#each presets as preset (preset.question)}
            <div>
                <h3>{preset.question}</h3>
                <ul>
                    {#each preset.options as option (option)}
                        <li>{option}</li>
                    {/each}
                </ul>
                <button on:click={() => runPreset(preset)}>Run</button>
            </div>
        {/each}
    </div>
</main>

<style>
    main {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        height: 100vh;
    }
</style>