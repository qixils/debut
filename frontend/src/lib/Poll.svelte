<script lang="ts">
    import type {PollStatus} from "$lib/app";

    export let poll: PollStatus;
    export let authToken: string | undefined;

    let votePercents: Map<string, string> = new Map(poll.options.map(option => [option.value, '0%']));
    $: {
        const totalVotes = poll.options.reduce((total, option) => total + option.votes, 0);
        poll.options.forEach(option => {
            votePercents.set(option.value, Math.floor(totalVotes === 0 ? 0 : ((option.votes / totalVotes) * 100)) + '%');
        });
    }

    async function handleClick(optionIndex: number) {
        if (!authToken) return;
        if (poll.hasVoted) return;
        if (optionIndex < 0 || optionIndex >= poll.options.length) return;
        const option = poll.options[optionIndex];
        option.votes++;
        poll.hasVoted = true;
        poll = poll; // svelte shenanigans
        let newPoll = await fetch('https://debut.qixils.dev/api/poll/vote?option=' + optionIndex, {
            method: 'POST',
            headers: {'Authorization': 'Bearer ' + authToken}
        }).then(res => res.json());
        if (newPoll && !newPoll.error) {
            poll = newPoll;
        }
    }

    let canVote: boolean;
    $: canVote = !poll.hasVoted && poll.active && authToken !== undefined;
</script>

<div id="container">
    <div class="poll">
        <h1 class="text-xl font-medium">{poll.question}</h1>
        {#if authToken === undefined}
            <p class="text-gray-700 font-light text-sm">Grant permissions to the extension to vote</p>
        {/if}
        <ul>
            {#each poll.options as option, i}
                {@const percent = votePercents.get(option.value)}
                <!-- TODO: dynamically load tailwind colors -->
                <!--suppress HtmlWrongAttributeValue (it is wrong; svelte docs condone this)-->
                <button class={canVote ? 'pointer-events-auto' : ''} disabled={!canVote} on:click={() => handleClick(i)} style={poll.hasVoted ? 'background: linear-gradient(90deg, rgba(253, 164, 175, .75) ' + percent + ', rgba(253, 164, 175, .25) ' + percent + ')' : ''}>
                    {option.value}
                </button>
            {/each}
        </ul>
    </div>
</div>

<style lang="postcss">
    .poll {
        @apply px-5 py-3 rounded-xl bg-rose-100/80 text-center w-full shadow-lg shadow-rose-300/50 backdrop-blur backdrop-brightness-125;
    }

    button {
        @apply block w-full p-3 my-1 rounded bg-rose-200/75 enabled:hover:ring-rose-400/75 enabled:hover:ring ring-offset-0 transition-shadow duration-200;
    }
</style>