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
            if (data.status !== undefined) {
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

<div class="full flex justify-center items-center bg-gradient-to-br from-rose-200 to-rose-300 pointer-events-none">
    <div id="transition" class="w-full max-w-xs mx-auto transition-opacity" style="opacity: {active ? '1' : '0'}; transform: {active ? 'translateY(0)' : 'translateY(-100%)'}">
        {#if dialogue_index < dialogue.length}
            <Dialogue index={dialogue_index} active={active} next={next_dialogue} />
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
