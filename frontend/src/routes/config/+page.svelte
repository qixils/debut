<script lang="ts">
    import Poll from "$lib/Poll.svelte";
    import type {PollStatus} from "src/app.js";
    import {PollPreset} from "src/app.js";
    import {onMount} from "svelte";

    let currentPreset: PollPreset | undefined;
    let currentPoll: PollStatus | undefined;
    let previousPoll: PollStatus | undefined;
    let presets: PollPreset[] = [
        new PollPreset("Hi", ["Hello", "Hi", "Hey"]), // TODO
    ];
    let question: string = "";
    let options: string = "";
    let authHeader: Headers | undefined;

    onMount(async () => {
        Twitch.ext.onAuthorized((auth) => {
            authHeader = new Headers({Authorization: "Bearer " + auth.token});
        });
        setInterval(async () => {
            // TODO: replace with pubsub listener
            if (authHeader === undefined) {
                return;
            }
            if (currentPoll === undefined) {
                return; // TODO: remove this if i ever publicly release this
            }
            currentPoll = await fetch("/api/poll/status", {headers: authHeader}).then(res => res.json());
        }, 1000)
    });

    async function closePoll() {
        if (authHeader === undefined) {
            alert("Not logged in");
            return;
        }
        previousPoll = await fetch("/api/poll/close", {method: "POST", headers: authHeader}).then(res => res.json()) as PollStatus;
        previousPoll.hasVoted = true; // disable voting
        let winner = previousPoll.winner;
        // remove winning option from preset
        if (currentPreset !== undefined) {
            currentPreset.options = currentPreset.options.filter(option => option !== winner);
        }
    }

    async function runPoll(question: string, options: string[]) {
        if (authHeader === undefined) {
            alert("Not logged in");
            return;
        }
        if (question === "") {
            alert("You need a question");
            return;
        }
        options = options.filter(option => option !== "");
        if (options.length < 2) {
            alert("You need at least 2 options");
            return;
        }
        currentPreset = undefined;
        let body = question + "\n" + options.join("\n");
        currentPoll = await fetch("/api/poll/create", {method: "POST", headers: authHeader, body: body}).then(res => res.json());
    }

    async function runPreset(preset: PollPreset) {
        if (authHeader === undefined) {
            alert("Not logged in");
            return;
        }
        if (preset.options.length < 2) {
            alert("This preset has run out of options");
            return;
        }
        currentPreset = preset;
        let body = preset.question + "\n" + preset.options.join("\n");
        currentPoll = await fetch("/api/poll/create", {method: "POST", headers: authHeader, body: body}).then(res => res.json());
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

    <div>
        <h2>Manual Poll</h2>
        <input type="text" placeholder="Question" bind:value={question} />
        <textarea placeholder="Options" bind:value={options} />
        <button on:click={() => runPoll(question, options.split("\n"))}>Run</button>
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