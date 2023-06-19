<script lang="ts">
    import Poll from "$lib/Poll.svelte";
    import type {PollStatus} from "src/app.js";

    let poll: PollStatus = {
        question: "lexi's fate???",
        options: [
            {value: "kill her", votes: 23},
            {value: "spare her", votes: 14}
        ],
        totalVotes: 37,
        winner: null,
        active: true,
        hasVoted: false,
    }
</script>

<div class="w-1/3 max-w-xs mx-auto fixed right-4 top-4 pointer-events-none">
    <Poll {poll} />
</div>
