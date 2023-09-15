<script lang="ts">
    import Poll from "$lib/Poll.svelte";
    import Dialogue from "$lib/Dialogue.svelte";
    import type {PollStatus} from "$lib/app";
    import {dialogue, placeholderPoll} from "$lib/app";
    import {onMount} from "svelte";

    let poll: PollStatus = placeholderPoll;
    let authToken: string | undefined;
    let authHeader: Headers | undefined;
    $: authHeader = authToken ? new Headers({"Authorization": `Bearer ${authToken}`}) : undefined;

    let dialogue_index = 0;
    function next_dialogue() {
        dialogue_index++;
    }

    onMount(async () => {
        // init authToken
        authToken = Twitch.ext.viewer.sessionToken;
        Twitch.ext.onAuthorized((auth) => {
            authToken = auth.token;
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
    let active: boolean;
    $: active = poll.active || (dialogue_index > 0 && dialogue_index < dialogue.length);
</script>

<div id="transition" class="w-1/3 max-w-xs mx-auto fixed right-4 top-4 pointer-events-none" style="opacity: {active ? '1' : '0'}; transform: {active ? 'translateY(0)' : 'translateY(-100%)'}">
    {#if dialogue_index < dialogue.length}
        <Dialogue index={dialogue_index} active={active} next={next_dialogue} />
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
