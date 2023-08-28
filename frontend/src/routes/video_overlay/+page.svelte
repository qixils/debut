<script lang="ts">
    import Poll from "$lib/Poll.svelte";
    import Dialogue from "$lib/Dialogue.svelte";
    import type {PollStatus} from "$lib/app";
    import {dialogue, placeholderPoll} from "$lib/app";
    import {onMount} from "svelte";

    let poll: PollStatus = placeholderPoll;
    let authHeader: Headers | undefined;
    let authToken: string | undefined;

    let dialogue_index = 0;
    function next_dialogue() {
        dialogue_index++;
    }

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
            if (data.status !== undefined && !data.status.error) {
                poll = data.status;
            }
        });
        // init currentPoll
        setTimeout(async () => {
            if (authHeader === undefined) {
                return;
            }
            let newPoll = await fetch("https://debut.qixils.dev/api/poll/status", {headers: authHeader}).then(res => res.json());
            if (!newPoll || newPoll.error) {
                return;
            }
            poll = newPoll;
        }, 1000);
        // setInterval(async () => {
        //     poll = fullPlaceholderPoll;
        // }, 1000);
    });
</script>

<div id="transition" class="w-1/3 max-w-xs mx-auto fixed right-4 top-4 pointer-events-none" style="opacity: {poll.active ? '1' : '0'}; transform: {poll.active ? 'translateY(0)' : 'translateY(-100%)'}">
    {#if dialogue_index < dialogue.length}
        <Dialogue index={dialogue_index} active={poll.active} next={next_dialogue} />
    {:else}
        <Poll {poll} {authToken} />
    {/if}
</div>

<style>
    #transition {
        transition-property: opacity, transform;
        transition-duration: 2s;
        transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
    }
</style>
