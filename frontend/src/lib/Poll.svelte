<script lang="ts">
    import type {PollStatus} from "$lib/app";

    export let poll: PollStatus;

    let votePercents: Map<string, string> = new Map(poll.options.map(option => [option.value, '0%']));

    function updateVotePercents() {
        const totalVotes = poll.options.reduce((total, option) => total + option.votes, 0);
        poll.options.forEach(option => {
            votePercents.set(option.value, Math.floor(totalVotes === 0 ? 0 : ((option.votes / totalVotes) * 100)) + '%');
        });
        votePercents = votePercents; // svelte shenanigans
    }

    function handleClick(optionIndex: number) {
        if (poll.hasVoted) return;
        if (optionIndex < 0 || optionIndex >= poll.options.length) return;
        const option = poll.options[optionIndex];
        option.votes++;
        updateVotePercents();
        poll.hasVoted = true;
        fetch('/api/poll/vote?option=' + optionIndex, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
                // TODO: twitch auth token
            }
        })
    }
</script>

<div id="container">
    <div class="poll {poll.active ? '' : 'opacity-0'}">
        <h1 class="text-xl font-medium">{poll.question}</h1>
        <ul>
            {#each poll.options as option, i}
                {@const percent = votePercents.get(option.value)}
                <!-- TODO: dynamically load tailwind colors -->
                <!--suppress HtmlWrongAttributeValue (it is wrong; svelte docs condone this)-->
                <button disabled={poll.hasVoted} on:click={() => handleClick(i)} style={poll.hasVoted ? 'background: linear-gradient(90deg, rgba(253, 164, 175, .75) ' + percent + ', rgba(253, 164, 175, .25) ' + percent + ')' : ''}>
                    {option.value}
                </button>
            {/each}
        </ul>
    </div>
</div>

<style lang="postcss">
    @keyframes fade-in {
        0% { opacity: 0; }
        100% { opacity: 1; }
    }

    #container {
        animation: fade-in .5s ease-in-out;
    }

    .poll {
        @apply px-5 py-3 rounded-xl bg-rose-100/75 text-center w-full shadow shadow-rose-300/50 transition-opacity duration-1000 backdrop-blur backdrop-brightness-125 pointer-events-auto;
    }

    button {
        @apply block w-full p-3 my-1 rounded bg-rose-200/75 enabled:hover:ring-rose-400/75 enabled:hover:ring ring-offset-0 transition-shadow duration-200;
    }
</style>