<script>
    import Poll from "$lib/Poll.svelte";
</script>

<div class="full flex justify-center items-center bg-gradient-to-br from-rose-200 to-rose-300 pointer-events-none">
    <div class="w-full max-w-xs mx-auto">
        <Poll question="lexi's fate???" options={[{value: "kill her", votes: 23}, {value: "spare her", votes: 14}]} />
    </div>
</div>
