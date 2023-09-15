<script lang="ts">
    import {dialogue} from "$lib/app.js";

    export let index: number;
    export let active: boolean;
    export let next: () => void;
</script>

<div class="rounded-xl bg-rose-100/80 shadow-lg shadow-rose-300/50 backdrop-blur backdrop-brightness-125 p-6 text-center">
    <div class="flex flex-col space-y-4">
        <p class="text-gray-700 text-sm">
            {#each dialogue[index][0].split("\n") as line, i}
                {#if i > 0}
                    <br />
                {/if}
                {line}
            {/each}
        </p>
        <button class="bg-rose-500 hover:bg-rose-600 text-white font-bold py-2 px-4 rounded {active ? 'pointer-events-auto' : ''}" on:click={next}>
            {dialogue[index][1]}
        </button>
    </div>
</div>