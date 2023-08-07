<script lang="ts">
    import Poll from "$lib/Poll.svelte";
    import type {PollStatus} from "$lib/app";
    import {PollPreset} from "$lib/app";
    import {onMount} from "svelte";

    let currentPreset: PollPreset | undefined;
    let currentPoll: PollStatus | undefined;
    let previousPoll: PollStatus | undefined;
    let presets: PollPreset[] = [
        new PollPreset("Hi", ["Hello", "Hi", "Hey"]), // TODO
        new PollPreset("Hi", ["Hello", "Hi", "Hey"]), // TODO
    ];
    let question: string = "";
    let options: string = "";
    let pollPreview: PollStatus;
    $: pollPreview = {
        question: question,
        options: options.split("\n").filter(option => option !== "").map(option => {
            return {
                value: option,
                votes: 0,
            }
        }),
        totalVotes: 0,
        winner: null,
        winnerIndex: null,
        active: true,
        hasVoted: true,
    }
    let authHeader: Headers | undefined;

    onMount(async () => {
        // init authHeader
        Twitch.ext.onAuthorized((auth) => {
            authHeader = new Headers({Authorization: "Bearer " + auth.token});
        });
        // listen to pubsub for poll updates
        Twitch.ext.listen("broadcast", (target, contentType, message) => {
            if (contentType !== "application/json") {
                return;
            }
            let data = JSON.parse(message);
            if (data.status !== undefined) {
                currentPoll = data.status;
            }
        });
        // init currentPoll
        setTimeout(async () => {
            if (authHeader === undefined) {
                return;
            }
            currentPoll = await fetch("/api/poll/status", {headers: authHeader}).then(res => res.json());
        }, 1000);
    });

    async function closePoll() {
        if (authHeader === undefined) {
            alert("Not logged in");
            return;
        }
        previousPoll = await fetch("/api/poll/close", {method: "POST", headers: authHeader}).then(res => res.json()) as PollStatus;
        previousPoll.hasVoted = true; // disable voting
        previousPoll = previousPoll; // force svelte update
        let winner = previousPoll.winner;
        // remove winning option from preset
        if (currentPreset !== undefined) {
            currentPreset.options = currentPreset.options.filter(option => option !== winner);
            presets = presets; // force svelte update
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
        if (options.length < 1) {
            alert("You need at least 1 option");
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
        if (preset.options.length < 1) {
            alert("This preset has run out of options");
            return;
        }
        currentPreset = preset;
        let body = preset.question + "\n" + preset.options.join("\n");
        currentPoll = await fetch("/api/poll/create", {method: "POST", headers: authHeader, body: body}).then(res => res.json());
    }
</script>

<main>
    {#if authHeader === undefined}
        <h1 class="text-red-500">Unauthorized</h1>
    {:else}
        <h1 class="text-green-500">Authorized</h1>
    {/if}

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
        <div class="presets">
            {#each presets as preset}
                <div class="preset">
                    <h3>{preset.question}</h3>
                    <ul>
                        {#each preset.options as option}
                            <li>{option}</li>
                        {/each}
                    </ul>
                    <button on:click={() => runPreset(preset)}>Run</button>
                </div>
            {/each}
        </div>
    </div>

    <div class="flex flex-row">
        <div class="mx-2">
            <h2>Manual Poll</h2>
            <p><input type="text" placeholder="Question" bind:value={question} /></p>
            <p><textarea placeholder="Options" bind:value={options} /></p>
            <button on:click={() => runPoll(question, options.split("\n"))}>Run</button>
        </div>
        <div>
            <Poll poll={pollPreview} />
        </div>
    </div>
</main>

<style lang="postcss">
    main {
        @apply bg-gradient-to-br from-rose-200 to-rose-300 p-4;
        display: flex;
        flex-direction: column;
        height: 100vh;
    }

    main > div {
        @apply bg-white/50 rounded-lg shadow-lg p-4 my-2;
    }

    h1 {
        @apply text-3xl font-bold;
    }

    h2 {
        @apply text-2xl font-bold;
    }

    h3 {
        @apply text-xl font-bold;
    }

    .presets {
        display: flex;
        flex-wrap: wrap;
        flex-direction: row;
    }

    .presets > div {
        @apply bg-white/50 rounded-lg shadow-md p-2 m-1;
        min-width: 8em;
    }

    .preset > h3 {
        @apply text-center;
    }

    .preset button {
        @apply mx-auto;
    }

    ul {
        @apply list-disc list-inside;
    }

    button {
        @apply bg-rose-500 hover:bg-rose-600 text-white font-bold py-2 px-4 m-2 rounded block;
    }

    input, textarea {
        width: 24em;
    }

    textarea {
        height: 12em;
    }
</style>