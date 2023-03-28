<script>
    import Poll from "$lib/Poll.svelte";
</script>

<div class="full flex justify-center items-center bg-gradient-to-br from-rose-200 to-rose-300">
    <div class="w-full max-w-xs mx-auto">
        <Poll question="Question!!!" options={["Die", "Fry", "Fly", "Guy"]} />
    </div>
</div>
