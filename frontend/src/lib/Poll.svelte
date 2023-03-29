<script lang="ts">
    import type {Option} from "src/app";

    export let question: string;
    export let options: Option[];

    let disabled = false;
    let active = true;
    let votePercents: Map<string, string> = new Map(options.map(option => [option.value, '0%']));

    function updateVotePercents() {
        const totalVotes = options.reduce((total, option) => total + option.votes, 0);
        options.forEach(option => {
            votePercents.set(option.value, Math.floor(totalVotes === 0 ? 0 : ((option.votes / totalVotes) * 100)) + '%');
        });
        votePercents = votePercents; // svelte shenanigans
    }

    function handleClick(optionValue: string) {
        if (disabled) return;
        const option = options.find(option => option.value === optionValue);
        if (!option) return;
        option.votes++;
        updateVotePercents();
        disabled = true;
        // TODO: send answer to server
    }
</script>

<div id="container">
    <div class="poll {active ? '' : 'opacity-0'}">
        <h1 class="text-xl font-medium">{question}</h1>
        <ul>
            {#each options as option}
                {@const percent = votePercents.get(option.value)}
                <!-- TODO: dynamically load tailwind colors -->
                <button {disabled} on:click={() => handleClick(option.value)} style={disabled ? 'background: linear-gradient(90deg, rgba(253, 164, 175, .75) ' + percent + ', rgba(253, 164, 175, .25) ' + percent + ')' : ''}>
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
        @apply p-5 pb-1 rounded-xl bg-rose-100/75 text-center w-full shadow shadow-rose-300/50 transition-opacity duration-1000 backdrop-blur backdrop-brightness-125;
    }

    button {
        @apply block w-full p-3 my-4 rounded bg-rose-200/75 enabled:hover:ring-rose-400/75 enabled:hover:ring ring-offset-0 transition-shadow duration-200;
    }
</style>