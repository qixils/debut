<script lang="ts">
    import Poll from "$lib/Poll.svelte";
    import type {PollStatus} from "$lib/app";
    import {onMount} from "svelte";

    let poll: PollStatus | undefined;
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
                poll = data.status;
            }
        });
        // init currentPoll
        setTimeout(async () => {
            if (authHeader === undefined) {
                return;
            }
            poll = await fetch("/api/poll/status", {headers: authHeader}).then(res => res.json());
        }, 1000);
    });
</script>

<div class="full flex justify-center items-center bg-gradient-to-br from-rose-200 to-rose-300 pointer-events-none">
    <div class="w-full max-w-xs mx-auto">
        {#if poll !== undefined}
            <Poll {poll} />
        {/if}
    </div>
</div>
