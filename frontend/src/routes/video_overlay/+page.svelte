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

{#if poll !== undefined}
    <div class="w-1/3 max-w-xs mx-auto fixed right-4 top-4 pointer-events-none">
        <Poll {poll} />
    </div>
{/if}
