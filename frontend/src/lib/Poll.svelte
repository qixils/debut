<script>
    export let question;
    export let options;
    let disabled = false;
    let active = true;

    function handleClick(option) {
        disabled = true;
        // TODO: send answer to server
    }
</script>

<div class={active ? '' : 'opacity-0'}>
    <h1 class="text-xl font-medium">{question}</h1>
    <ul>
        {#each options as option}
            <!-- TODO: display live votes -->
            <button {disabled} on:click={() => handleClick(option)}>
                {option}
            </button>
        {/each}
    </ul>
</div>

<style>
    div {
        @apply p-5 pb-1 rounded-xl bg-rose-200/75 text-center w-full shadow shadow-rose-300/50 transition-opacity duration-1000 backdrop-blur backdrop-brightness-125;
        /* TODO: pink gradient */
    }

    button {
        @apply block w-full p-3 my-4 rounded bg-rose-100/75 border-transparent enabled:hover:border-rose-400/75 border-4 transition-colors duration-200 disabled:bg-rose-100/50;
    }
</style>