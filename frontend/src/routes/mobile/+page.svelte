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
            if (data.status !== undefined) {
                poll = data.status;
            }
        });
        // init currentPoll
        setTimeout(async () => {
            if (authHeader === undefined) {
                return;
            }
            let newPoll = await fetch("/api/poll/status", {headers: authHeader}).then(res => res.json());
            if (newPoll && !newPoll.error) {
                poll = newPoll;
            }
        }, 1000);
        // setInterval(async () => {
        //     poll = fullPlaceholderPoll;
        // }, 1000);
    });
</script>

<div class="full flex justify-center items-center bg-gradient-to-br from-rose-200 to-rose-300 pointer-events-none">
    <div id="transition" class="w-full max-w-xs mx-auto transition-opacity" style="opacity: {poll.active ? '1' : '0'}; transform: {poll.active ? 'translateY(0)' : 'translateY(-100%)'}">
        {#if dialogue_index < dialogue.length}
            <Dialogue index={dialogue_index} active={poll.active} next={next_dialogue} />
        {:else}
            <Poll {poll} {authToken} />
        {/if}
    </div>
</div>

<style>
    #transition {
        transition-property: opacity, transform;
        transition-duration: 2s;
        transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
    }
</style>
