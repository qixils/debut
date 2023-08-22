<script lang="ts">
    import Poll from "$lib/Poll.svelte";
    import type {PollStatus} from "$lib/app";
    import {placeholderPoll} from "$lib/app";
    import {onMount} from "svelte";

    let poll: PollStatus = placeholderPoll;
    let authHeader: Headers | undefined;
    let authToken: string | undefined;

    onMount(async () => {
        // init authHeader
        Twitch.ext.onAuthorized((auth) => {
            authToken = auth.token;
            authHeader = new Headers({Authorization: "Bearer " + authToken});
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

<div class="w-1/3 max-w-xs mx-auto fixed right-4 top-4 pointer-events-none transition-opacity duration-1000 {poll.active ? 'opacity-100' : 'opacity-0'}">
    <Poll {poll} {authToken} />
</div>
