# Google AI Studio Wrapper

================================================
FILE: README.md
================================================
A reverse-engineered asynchronous python wrapper for [Google Gemini](https://gemini.google.com) web app (formerly Bard).

## Features

- **Persistent Cookies** - Automatically refreshes cookies in background. Optimized for always-on services.
- **Image Generation** - Natively supports generating and modifying images with natural language.
- **System Prompt** - Supports customizing model's system prompt with [Gemini Gems](https://gemini.google.com/gems/view).
- **Extension Support** - Supports generating contents with [Gemini extensions](https://gemini.google.com/extensions) on, like YouTube and Gmail.
- **Classified Outputs** - Categorizes texts, thoughts, web images and AI generated images in the response.
- **Official Flavor** - Provides a simple and elegant interface inspired by [Google Generative AI](https://ai.google.dev/tutorials/python_quickstart)'s official API.
- **Asynchronous** - Utilizes `asyncio` to run generating tasks and return outputs efficiently.

## Table of Contents

- [Features](#features)
- [Table of Contents](#table-of-contents)
- [Installation](#installation)
- [Authentication](#authentication)
- [Usage](#usage)
    - [Initialization](#initialization)
    - [Generate contents](#generate-contents)
    - [Generate contents with files](#generate-contents-with-files)
    - [Conversations across multiple turns](#conversations-across-multiple-turns)
    - [Continue previous conversations](#continue-previous-conversations)
    - [Select language model](#select-language-model)
    - [Apply system prompt with Gemini Gems](#apply-system-prompt-with-gemini-gems)
    - [Retrieve model's thought process](#retrieve-models-thought-process)
    - [Retrieve images in response](#retrieve-images-in-response)
    - [Generate images with Imagen4](#generate-images-with-imagen4)
    - [Generate contents with Gemini extensions](#generate-contents-with-gemini-extensions)
    - [Check and switch to other reply candidates](#check-and-switch-to-other-reply-candidates)
    - [Logging Configuration](#logging-configuration)
- [References](#references)
- [Stargazers](#stargazers)

## Installation

> [!NOTE]
>
> This package requires Python 3.10 or higher.

Install/update the package with pip.

```sh
pip install -U gemini_webapi
```

Optionally, package offers a way to automatically import cookies from your local browser. To enable this feature, install `browser-cookie3` as well. Supported platforms and browsers can be found [here](https://github.com/borisbabic/browser_cookie3?tab=readme-ov-file#contribute).

```sh
pip install -U browser-cookie3
```

## Authentication

> [!TIP]
>
> If `browser-cookie3` is installed, you can skip this step and go directly to [usage](#usage) section. Just make sure you have logged in to <https://gemini.google.com> in your browser.

- Go to <https://gemini.google.com> and login with your Google account
- Press F12 for web inspector, go to `Network` tab and refresh the page
- Click any request and copy cookie values of `__Secure-1PSID` and `__Secure-1PSIDTS`

> [!NOTE]
>
> If your application is deployed in a containerized environment (e.g. Docker), you may want to persist the cookies with a volume to avoid re-authentication every time the container rebuilds.
>
> Here's part of a sample `docker-compose.yml` file:

```yaml
services:
    main:
        volumes:
            - ./gemini_cookies:/usr/local/lib/python3.12/site-packages/gemini_webapi/utils/temp
```

> [!NOTE]
>
> API's auto cookie refreshing feature doesn't require `browser-cookie3`, and by default is enabled. It allows you to keep the API service running without worrying about cookie expiration.
>
> This feature may cause that you need to re-login to your Google account in the browser. This is an expected behavior and won't affect the API's functionality.
>
> To avoid such result, it's recommended to get cookies from a separate browser session and close it as asap for best utilization (e.g. a fresh login in browser's private mode). More details can be found [here](https://github.com/HanaokaYuzu/Gemini-API/issues/6).

## Usage

### Initialization

Import required packages and initialize a client with your cookies obtained from the previous step. After a successful initialization, the API will automatically refresh `__Secure-1PSIDTS` in background as long as the process is alive.

```python
import asyncio
from gemini_webapi import GeminiClient

# Replace "COOKIE VALUE HERE" with your actual cookie values.
# Leave Secure_1PSIDTS empty if it's not available for your account.
Secure_1PSID = "COOKIE VALUE HERE"
Secure_1PSIDTS = "COOKIE VALUE HERE"

async def main():
    # If browser-cookie3 is installed, simply use `client = GeminiClient()`
    client = GeminiClient(Secure_1PSID, Secure_1PSIDTS, proxy=None)
    await client.init(timeout=30, auto_close=False, close_delay=300, auto_refresh=True)

asyncio.run(main())
```

> [!TIP]
>
> `auto_close` and `close_delay` are optional arguments for automatically closing the client after a certain period of inactivity. This feature is disabled by default. In an always-on service like chatbot, it's recommended to set `auto_close` to `True` combined with reasonable seconds of `close_delay` for better resource management.

### Generate contents

Ask a single-turn question by calling `GeminiClient.generate_content`, which returns a `gemini_webapi.ModelOutput` object containing the generated text, images, thoughts, and conversation metadata.

```python
async def main():
    response = await client.generate_content("Hello World!")
    print(response.text)

asyncio.run(main())
```

> [!TIP]
>
> Simply use `print(response)` to get the same output if you just want to see the response text

### Generate contents with files

Gemini supports file input, including images and documents. Optionally, you can pass files as a list of paths in `str` or `pathlib.Path` to `GeminiClient.generate_content` together with text prompt.

```python
async def main():
    response = await client.generate_content(
            "Introduce the contents of these two files. Is there any connection between them?",
            files=["assets/sample.pdf", Path("assets/banner.png")],
        )
    print(response.text)

asyncio.run(main())
```

### Conversations across multiple turns

If you want to keep conversation continuous, please use `GeminiClient.start_chat` to create a `gemini_webapi.ChatSession` object and send messages through it. The conversation history will be automatically handled and get updated after each turn.

```python
async def main():
    chat = client.start_chat()
    response1 = await chat.send_message(
        "Introduce the contents of these two files. Is there any connection between them?",
        files=["assets/sample.pdf", Path("assets/banner.png")],
    )
    print(response1.text)
    response2 = await chat.send_message(
        "Use image generation tool to modify the banner with another font and design."
    )
    print(response2.text, response2.images, sep="\n\n----------------------------------\n\n")

asyncio.run(main())
```

> [!TIP]
>
> Same as `GeminiClient.generate_content`, `ChatSession.send_message` also accepts `image` as an optional argument.

### Continue previous conversations

To manually retrieve previous conversations, you can pass previous `ChatSession`'s metadata to `GeminiClient.start_chat` when creating a new `ChatSession`. Alternatively, you can persist previous metadata to a file or db if you need to access them after the current Python process has exited.

```python
async def main():
    # Start a new chat session
    chat = client.start_chat()
    response = await chat.send_message("Fine weather today")

    # Save chat's metadata
    previous_session = chat.metadata

    # Load the previous conversation
    previous_chat = client.start_chat(metadata=previous_session)
    response = await previous_chat.send_message("What was my previous message?")
    print(response)

asyncio.run(main())
```

### Select language model

You can specify which language model to use by passing `model` argument to `GeminiClient.generate_content` or `GeminiClient.start_chat`. The default value is `unspecified`.

Currently available models (as of June 12, 2025):

- `unspecified` - Default model
- `gemini-2.5-flash` - Gemini 2.5 Flash
- `gemini-2.5-pro` - Gemini 2.5 Pro (daily usage limit imposed)

Deprecated models (yet still working):

- `gemini-2.0-flash` - Gemini 2.0 Flash
- `gemini-2.0-flash-thinking` - Gemini 2.0 Flash Thinking

```python
from gemini_webapi.constants import Model

async def main():
    response1 = await client.generate_content(
        "What's you language model version? Reply version number only.",
        model=Model.G_2_5_FLASH,
    )
    print(f"Model version ({Model.G_2_5_FLASH.model_name}): {response1.text}")

    chat = client.start_chat(model="gemini-2.5-pro")
    response2 = await chat.send_message("What's you language model version? Reply version number only.")
    print(f"Model version (gemini-2.5-pro): {response2.text}")

asyncio.run(main())
```

### Apply system prompt with Gemini Gems

System prompt can be applied to conversations via [Gemini Gems](https://gemini.google.com/gems/view). To use a gem, you can pass `gem` argument to `GeminiClient.generate_content` or `GeminiClient.start_chat`. `gem` can be either a string of gem id or a `gemini_webapi.Gem` object. Only one gem can be applied to a single conversation.

```python
async def main():
    # Fetch all gems for the current account, including both predefined and user-created ones
    await client.fetch_gems()

    # Once fetched, gems will be cached in `GeminiClient.gems`
    gems = client.gems

    # Get the gem you want to use
    system_gems = gems.filter(predefined=True)
    coding_partner = system_gems.get(id="coding-partner")

    response1 = await client.generate_content(
        "what's your system prompt?",
        model=Model.G_2_5_FLASH,
        gem=coding_partner,
    )
    print(response1.text)

    # Another example with a user-created custom gem
    # Gem ids are consistent strings. Store them somewhere to avoid fetching gems every time
    your_gem = gems.get(name="Your Gem Name")
    your_gem_id = your_gem.id
    chat = client.start_chat(gem=your_gem_id)
    response2 = await chat.send_message("what's your system prompt?")
    print(response2)
```

### Retrieve model's thought process

When using models with thinking capabilities, the model's thought process will be populated in `ModelOutput.thoughts`.

```python
async def main():
    response = await client.generate_content(
            "What's 1+1?", model="gemini-2.5-pro"
        )
    print(response.thoughts)
    print(response.text)

asyncio.run(main())
```

### Retrieve images in response

Images in the API's output are stored as a list of `gemini_webapi.Image` objects. You can access the image title, URL, and description by calling `Image.title`, `Image.url` and `Image.alt` respectively.

```python
async def main():
    response = await client.generate_content("Send me some pictures of cats")
    for image in response.images:
        print(image, "\n\n----------------------------------\n")

asyncio.run(main())
```

### Generate images with Imagen4

You can ask Gemini to generate and modify images with Imagen4, Google's latest AI image generator, simply by natural language.

> [!IMPORTANT]
>
> Google has some limitations on the image generation feature in Gemini, so its availability could be different per region/account. Here's a summary copied from [official documentation](https://support.google.com/gemini/answer/14286560) (as of March 19th, 2025):
>
> > This feature’s availability in any specific Gemini app is also limited to the supported languages and countries of that app.
> >
> > For now, this feature isn’t available to users under 18.
> >
> > To use this feature, you must be signed in to Gemini Apps.

You can save images returned from Gemini to local by calling `Image.save()`. Optionally, you can specify the file path and file name by passing `path` and `filename` arguments to the function and skip images with invalid file names by passing `skip_invalid_filename=True`. Works for both `WebImage` and `GeneratedImage`.

```python
async def main():
    response = await client.generate_content("Generate some pictures of cats")
    for i, image in enumerate(response.images):
        await image.save(path="temp/", filename=f"cat_{i}.png", verbose=True)
        print(image, "\n\n----------------------------------\n")

asyncio.run(main())
```

> [!NOTE]
>
> by default, when asked to send images (like the previous example), Gemini will send images fetched from web instead of generating images with AI model, unless you specifically require to "generate" images in your prompt. In this package, web images and generated images are treated differently as `WebImage` and `GeneratedImage`, and will be automatically categorized in the output.

### Generate contents with Gemini extensions

> [!IMPORTANT]
>
> To access Gemini extensions in API, you must activate them on the [Gemini website](https://gemini.google.com/extensions) first. Same as image generation, Google also has limitations on the availability of Gemini extensions. Here's a summary copied from [official documentation](https://support.google.com/gemini/answer/13695044) (as of March 19th, 2025):
>
> > To connect apps to Gemini, you must have​​​​ Gemini Apps Activity on.
> >
> > To use this feature, you must be signed in to Gemini Apps.
> >
> > Important: If you’re under 18, Google Workspace and Maps apps currently only work with English prompts in Gemini.

After activating extensions for your account, you can access them in your prompts either by natural language or by starting your prompt with "@" followed by the extension keyword.

```python
async def main():
    response1 = await client.generate_content("@Gmail What's the latest message in my mailbox?")
    print(response1, "\n\n----------------------------------\n")

    response2 = await client.generate_content("@Youtube What's the latest activity of Taylor Swift?")
    print(response2, "\n\n----------------------------------\n")

asyncio.run(main())
```

> [!NOTE]
>
> For the available regions limitation, it actually only requires your Google account's **preferred language** to be set to one of the three supported languages listed above. You can change your language settings [here](https://myaccount.google.com/language).

### Check and switch to other reply candidates

A response from Gemini sometimes contains multiple reply candidates with different generated contents. You can check all candidates and choose one to continue the conversation. By default, the first candidate will be chosen.

```python
async def main():
    # Start a conversation and list all reply candidates
    chat = client.start_chat()
    response = await chat.send_message("Recommend a science fiction book for me.")
    for candidate in response.candidates:
        print(candidate, "\n\n----------------------------------\n")

    if len(response.candidates) > 1:
        # Control the ongoing conversation flow by choosing candidate manually
        new_candidate = chat.choose_candidate(index=1)  # Choose the second candidate here
        followup_response = await chat.send_message("Tell me more about it.")  # Will generate contents based on the chosen candidate
        print(new_candidate, followup_response, sep="\n\n----------------------------------\n\n")
    else:
        print("Only one candidate available.")

asyncio.run(main())
```

### Logging Configuration

This package uses [loguru](https://loguru.readthedocs.io/en/stable/) for logging, and exposes a function `set_log_level` to control log level. You can set log level to one of the following values: `DEBUG`, `INFO`, `WARNING`, `ERROR` and `CRITICAL`. The default value is `INFO`.

```python
from gemini_webapi import set_log_level

set_log_level("DEBUG")
```

> [!NOTE]
>
> Calling `set_log_level` for the first time will **globally** remove all existing loguru handlers. You may want to configure logging directly with loguru to avoid this issue and have more advanced control over logging behaviors.

## References

[Google AI Studio](https://ai.google.dev/tutorials/ai-studio_quickstart)

[acheong08/Bard](https://github.com/acheong08/Bard)

## Stargazers

<p align="center">
    <a href="https://star-history.com/#HanaokaYuzu/Gemini-API">
        <img src="https://api.star-history.com/svg?repos=HanaokaYuzu/Gemini-API&type=Date" width="75%" alt="Star History Chart"></a>
</p>



================================================
FILE: src/gemini_webapi/client.py
================================================
import asyncio
import functools
import itertools
import re
from asyncio import Task
from pathlib import Path
from typing import Any, Optional

import orjson as json
from httpx import AsyncClient, ReadTimeout

from .constants import Endpoint, ErrorCode, Headers, Model
from .exceptions import (
AuthError,
APIError,
ImageGenerationError,
TimeoutError,
GeminiError,
UsageLimitExceeded,
ModelInvalid,
TemporarilyBlocked,
)
from .types import WebImage, GeneratedImage, Candidate, ModelOutput, Gem, GemJar
from .utils import (
upload_file,
parse_file_name,
rotate_1psidts,
get_access_token,
load_browser_cookies,
rotate_tasks,
logger,
)


def running(retry: int = 0) -> callable:
"""
Decorator to check if client is running before making a request.

    Parameters
    ----------
    retry: `int`, optional
        Max number of retries when `gemini_webapi.APIError` is raised.
    """

    def decorator(func):
        @functools.wraps(func)
        async def wrapper(client: "GeminiClient", *args, retry=retry, **kwargs):
            try:
                if not client.running:
                    await client.init(
                        timeout=client.timeout,
                        auto_close=client.auto_close,
                        close_delay=client.close_delay,
                        auto_refresh=client.auto_refresh,
                        refresh_interval=client.refresh_interval,
                        verbose=False,
                    )
                    if client.running:
                        return await func(client, *args, **kwargs)

                    # Should not reach here
                    raise APIError(
                        f"Invalid function call: GeminiClient.{func.__name__}. Client initialization failed."
                    )
                else:
                    return await func(client, *args, **kwargs)
            except APIError as e:
                # Image generation takes too long, only retry once
                if isinstance(e, ImageGenerationError):
                    retry = min(1, retry)

                if retry > 0:
                    await asyncio.sleep(1)
                    return await wrapper(client, *args, retry=retry - 1, **kwargs)

                raise

        return wrapper

    return decorator


class GeminiClient:
"""
Async httpx client interface for gemini.google.com.

    `secure_1psid` must be provided unless the optional dependency `browser-cookie3` is installed and
    you have logged in to google.com in your local browser.

    Parameters
    ----------
    secure_1psid: `str`, optional
        __Secure-1PSID cookie value.
    secure_1psidts: `str`, optional
        __Secure-1PSIDTS cookie value, some google accounts don't require this value, provide only if it's in the cookie list.
    proxy: `str`, optional
        Proxy URL.
    kwargs: `dict`, optional
        Additional arguments which will be passed to the http client.
        Refer to `httpx.AsyncClient` for more information.

    Raises
    ------
    `ValueError`
        If `browser-cookie3` is installed but cookies for google.com are not found in your local browser storage.
    """

    __slots__ = [
        "cookies",
        "proxy",
        "running",
        "client",
        "access_token",
        "timeout",
        "auto_close",
        "close_delay",
        "close_task",
        "auto_refresh",
        "refresh_interval",
        "_gems",
        "kwargs",
    ]

    def __init__(
        self,
        secure_1psid: str | None = None,
        secure_1psidts: str | None = None,
        proxy: str | None = None,
        **kwargs,
    ):
        self.cookies = {}
        self.proxy = proxy
        self.running: bool = False
        self.client: AsyncClient | None = None
        self.access_token: str | None = None
        self.timeout: float = 300
        self.auto_close: bool = False
        self.close_delay: float = 300
        self.close_task: Task | None = None
        self.auto_refresh: bool = True
        self.refresh_interval: float = 540
        self._gems: GemJar | None = None
        self.kwargs = kwargs

        # Validate cookies
        if secure_1psid:
            self.cookies["__Secure-1PSID"] = secure_1psid
            if secure_1psidts:
                self.cookies["__Secure-1PSIDTS"] = secure_1psidts
        else:
            try:
                cookies = load_browser_cookies(domain_name="google.com")
                if not (cookies and cookies.get("__Secure-1PSID")):
                    raise ValueError(
                        "Failed to load cookies from local browser. Please pass cookie values manually."
                    )
            except ImportError:
                pass

    async def init(
        self,
        timeout: float = 30,
        auto_close: bool = False,
        close_delay: float = 300,
        auto_refresh: bool = True,
        refresh_interval: float = 540,
        verbose: bool = True,
    ) -> None:
        """
        Get SNlM0e value as access token. Without this token posting will fail with 400 bad request.

        Parameters
        ----------
        timeout: `float`, optional
            Request timeout of the client in seconds. Used to limit the max waiting time when sending a request.
        auto_close: `bool`, optional
            If `True`, the client will close connections and clear resource usage after a certain period
            of inactivity. Useful for always-on services.
        close_delay: `float`, optional
            Time to wait before auto-closing the client in seconds. Effective only if `auto_close` is `True`.
        auto_refresh: `bool`, optional
            If `True`, will schedule a task to automatically refresh cookies in the background.
        refresh_interval: `float`, optional
            Time interval for background cookie refresh in seconds. Effective only if `auto_refresh` is `True`.
        verbose: `bool`, optional
            If `True`, will print more infomation in logs.
        """

        try:
            access_token, valid_cookies = await get_access_token(
                base_cookies=self.cookies, proxy=self.proxy, verbose=verbose
            )

            self.client = AsyncClient(
                http2=True,
                timeout=timeout,
                proxy=self.proxy,
                follow_redirects=True,
                headers=Headers.GEMINI.value,
                cookies=valid_cookies,
                **self.kwargs,
            )
            self.access_token = access_token
            self.cookies = valid_cookies
            self.running = True

            self.timeout = timeout
            self.auto_close = auto_close
            self.close_delay = close_delay
            if self.auto_close:
                await self.reset_close_task()

            self.auto_refresh = auto_refresh
            self.refresh_interval = refresh_interval
            if task := rotate_tasks.get(self.cookies["__Secure-1PSID"]):
                task.cancel()
            if self.auto_refresh:
                rotate_tasks[self.cookies["__Secure-1PSID"]] = asyncio.create_task(
                    self.start_auto_refresh()
                )

            if verbose:
                logger.success("Gemini client initialized successfully.")
        except Exception:
            await self.close()
            raise

    async def close(self, delay: float = 0) -> None:
        """
        Close the client after a certain period of inactivity, or call manually to close immediately.

        Parameters
        ----------
        delay: `float`, optional
            Time to wait before closing the client in seconds.
        """

        if delay:
            await asyncio.sleep(delay)

        self.running = False

        if self.close_task:
            self.close_task.cancel()
            self.close_task = None

        if self.client:
            await self.client.aclose()

    async def reset_close_task(self) -> None:
        """
        Reset the timer for closing the client when a new request is made.
        """

        if self.close_task:
            self.close_task.cancel()
            self.close_task = None
        self.close_task = asyncio.create_task(self.close(self.close_delay))

    async def start_auto_refresh(self) -> None:
        """
        Start the background task to automatically refresh cookies.
        """

        while True:
            try:
                new_1psidts = await rotate_1psidts(self.cookies, self.proxy)
            except AuthError:
                if task := rotate_tasks.get(self.cookies["__Secure-1PSID"]):
                    task.cancel()
                logger.warning(
                    "Failed to refresh cookies. Background auto refresh task canceled."
                )

            logger.debug(f"Cookies refreshed. New __Secure-1PSIDTS: {new_1psidts}")
            if new_1psidts:
                self.cookies["__Secure-1PSIDTS"] = new_1psidts
            await asyncio.sleep(self.refresh_interval)

    @property
    def gems(self) -> GemJar:
        """
        Returns a `GemJar` object containing cached gems.
        Only available after calling `GeminiClient.fetch_gems()`.

        Returns
        -------
        :class:`GemJar`
            Refer to `gemini_webapi.types.GemJar`.

        Raises
        ------
        `RuntimeError`
            If `GeminiClient.fetch_gems()` has not been called before accessing this property.
        """

        if self._gems is None:
            raise RuntimeError(
                "Gems not fetched yet. Call `GeminiClient.fetch_gems()` method to fetch gems from gemini.google.com."
            )

        return self._gems

    @running(retry=2)
    async def fetch_gems(self, **kwargs) -> GemJar:
        """
        Get a list of available gems from gemini, including system predefined gems and user-created custom gems.

        Note that network request will be sent every time this method is called.
        Once the gems are fetched, they will be cached and accessible via `GeminiClient.gems` property.

        Returns
        -------
        :class:`GemJar`
            Refer to `gemini_webapi.types.GemJar`.
        """

        try:
            response = await self.client.post(
                Endpoint.BATCH_EXEC,
                data={
                    "at": self.access_token,
                    "f.req": json.dumps(
                        [
                            [
                                ["CNgdBe", '[2,["en"],0]', None, "custom"],
                                ["CNgdBe", '[3,["en"],0]', None, "system"],
                            ]
                        ]
                    ).decode(),
                },
                **kwargs,
            )
        except ReadTimeout:
            raise TimeoutError(
                "Fetch gems request timed out, please try again. If the problem persists, "
                "consider setting a higher `timeout` value when initializing GeminiClient."
            )

        if response.status_code != 200:
            raise APIError(
                f"Failed to fetch gems. Request failed with status code {response.status_code}"
            )
        else:
            try:
                response_json = json.loads(response.text.split("\n")[2])

                predefined_gems, custom_gems = [], []

                for part in response_json:
                    if part[-1] == "system":
                        predefined_gems = json.loads(part[2])[2]
                    elif part[-1] == "custom":
                        if custom_gems_container := json.loads(part[2]):
                            custom_gems = custom_gems_container[2]

                if not predefined_gems and not custom_gems:
                    raise Exception
            except Exception:
                await self.close()
                logger.debug(f"Invalid response: {response.text}")
                raise APIError(
                    "Failed to fetch gems. Invalid response data received. Client will try to re-initialize on next request."
                )

        self._gems = GemJar(
            itertools.chain(
                (
                    (
                        gem[0],
                        Gem(
                            id=gem[0],
                            name=gem[1][0],
                            description=gem[1][1],
                            prompt=gem[2] and gem[2][0] or None,
                            predefined=True,
                        ),
                    )
                    for gem in predefined_gems
                ),
                (
                    (
                        gem[0],
                        Gem(
                            id=gem[0],
                            name=gem[1][0],
                            description=gem[1][1],
                            prompt=gem[2] and gem[2][0] or None,
                            predefined=False,
                        ),
                    )
                    for gem in custom_gems
                ),
            )
        )

        return self._gems

    @running(retry=2)
    async def generate_content(
        self,
        prompt: str,
        files: list[str | Path] | None = None,
        model: Model | str = Model.UNSPECIFIED,
        gem: Gem | str | None = None,
        chat: Optional["ChatSession"] = None,
        **kwargs,
    ) -> ModelOutput:
        """
        Generates contents with prompt.

        Parameters
        ----------
        prompt: `str`
            Prompt provided by user.
        files: `list[str | Path]`, optional
            List of file paths to be attached.
        model: `Model` | `str`, optional
            Specify the model to use for generation.
            Pass either a `gemini_webapi.constants.Model` enum or a model name string.
        gem: `Gem | str`, optional
            Specify a gem to use as system prompt for the chat session.
            Pass either a `gemini_webapi.types.Gem` object or a gem id string.
        chat: `ChatSession`, optional
            Chat data to retrieve conversation history. If None, will automatically generate a new chat id when sending post request.
        kwargs: `dict`, optional
            Additional arguments which will be passed to the post request.
            Refer to `httpx.AsyncClient.request` for more information.

        Returns
        -------
        :class:`ModelOutput`
            Output data from gemini.google.com, use `ModelOutput.text` to get the default text reply, `ModelOutput.images` to get a list
            of images in the default reply, `ModelOutput.candidates` to get a list of all answer candidates in the output.

        Raises
        ------
        `AssertionError`
            If prompt is empty.
        `gemini_webapi.TimeoutError`
            If request timed out.
        `gemini_webapi.GeminiError`
            If no reply candidate found in response.
        `gemini_webapi.APIError`
            - If request failed with status code other than 200.
            - If response structure is invalid and failed to parse.
        """

        assert prompt, "Prompt cannot be empty."

        if not isinstance(model, Model):
            model = Model.from_name(model)

        if isinstance(gem, Gem):
            gem = gem.id

        if self.auto_close:
            await self.reset_close_task()

        try:
            response = await self.client.post(
                Endpoint.GENERATE.value,
                headers=model.model_header,
                data={
                    "at": self.access_token,
                    "f.req": json.dumps(
                        [
                            None,
                            json.dumps(
                                [
                                    files
                                    and [
                                        prompt,
                                        0,
                                        None,
                                        [
                                            [
                                                [await upload_file(file, self.proxy)],
                                                parse_file_name(file),
                                            ]
                                            for file in files
                                        ],
                                    ]
                                    or [prompt],
                                    None,
                                    chat and chat.metadata,
                                ]
                                + (gem and [None] * 16 + [gem] or [])
                            ).decode(),
                        ]
                    ).decode(),
                },
                **kwargs,
            )
        except ReadTimeout:
            raise TimeoutError(
                "Generate content request timed out, please try again. If the problem persists, "
                "consider setting a higher `timeout` value when initializing GeminiClient."
            )

        if response.status_code != 200:
            await self.close()
            raise APIError(
                f"Failed to generate contents. Request failed with status code {response.status_code}"
            )
        else:
            try:
                response_json = json.loads(response.text.split("\n")[2])

                body = None
                body_index = 0
                for part_index, part in enumerate(response_json):
                    try:
                        main_part = json.loads(part[2])
                        if main_part[4]:
                            body_index, body = part_index, main_part
                            break
                    except (IndexError, TypeError, ValueError):
                        continue

                if not body:
                    raise Exception
            except Exception:
                await self.close()

                try:
                    match ErrorCode(response_json[0][5][2][0][1][0]):
                        case ErrorCode.USAGE_LIMIT_EXCEEDED:
                            raise UsageLimitExceeded(
                                f"Failed to generate contents. Usage limit of {model.model_name} model has exceeded. Please try switching to another model."
                            )
                        case ErrorCode.MODEL_HEADER_INVALID:
                            raise ModelInvalid(
                                "Failed to generate contents. The specified model is not available. Please update gemini_webapi to the latest version. "
                                "If the error persists and is caused by the package, please report it on GitHub."
                            )
                        case ErrorCode.IP_TEMPORARILY_BLOCKED:
                            raise TemporarilyBlocked(
                                "Failed to generate contents. Your IP address is temporarily blocked by Google. Please try using a proxy or waiting for a while."
                            )
                        case _:
                            raise Exception
                except GeminiError:
                    raise
                except Exception:
                    logger.debug(f"Invalid response: {response.text}")
                    raise APIError(
                        "Failed to generate contents. Invalid response data received. Client will try to re-initialize on next request."
                    )

            try:
                candidates = []
                for candidate_index, candidate in enumerate(body[4]):
                    text = candidate[1][0]
                    if re.match(
                        r"^http://googleusercontent\.com/card_content/\d+", text
                    ):
                        text = candidate[22] and candidate[22][0] or text

                    try:
                        thoughts = candidate[37][0][0]
                    except (TypeError, IndexError):
                        thoughts = None

                    web_images = (
                        candidate[12]
                        and candidate[12][1]
                        and [
                            WebImage(
                                url=web_image[0][0][0],
                                title=web_image[7][0],
                                alt=web_image[0][4],
                                proxy=self.proxy,
                            )
                            for web_image in candidate[12][1]
                        ]
                        or []
                    )

                    generated_images = []
                    if candidate[12] and candidate[12][7] and candidate[12][7][0]:
                        img_body = None
                        for img_part_index, part in enumerate(response_json):
                            if img_part_index < body_index:
                                continue

                            try:
                                img_part = json.loads(part[2])
                                if img_part[4][candidate_index][12][7][0]:
                                    img_body = img_part
                                    break
                            except (IndexError, TypeError, ValueError):
                                continue

                        if not img_body:
                            raise ImageGenerationError(
                                "Failed to parse generated images. Please update gemini_webapi to the latest version. "
                                "If the error persists and is caused by the package, please report it on GitHub."
                            )

                        img_candidate = img_body[4][candidate_index]

                        text = re.sub(
                            r"http://googleusercontent\.com/image_generation_content/\d+",
                            "",
                            img_candidate[1][0],
                        ).rstrip()

                        generated_images = [
                            GeneratedImage(
                                url=generated_image[0][3][3],
                                title=f"[Generated Image {generated_image[3][6]}]",
                                alt=len(generated_image[3][5]) > image_index
                                and generated_image[3][5][image_index]
                                or generated_image[3][5][0],
                                proxy=self.proxy,
                                cookies=self.cookies,
                            )
                            for image_index, generated_image in enumerate(
                                img_candidate[12][7][0]
                            )
                        ]

                    candidates.append(
                        Candidate(
                            rcid=candidate[0],
                            text=text,
                            thoughts=thoughts,
                            web_images=web_images,
                            generated_images=generated_images,
                        )
                    )
                if not candidates:
                    raise GeminiError(
                        "Failed to generate contents. No output data found in response."
                    )

                output = ModelOutput(metadata=body[1], candidates=candidates)
            except (TypeError, IndexError):
                logger.debug(f"Invalid response: {response.text}")
                raise APIError(
                    "Failed to parse response body. Data structure is invalid."
                )

            if isinstance(chat, ChatSession):
                chat.last_output = output

            return output

    def start_chat(self, **kwargs) -> "ChatSession":
        """
        Returns a `ChatSession` object attached to this client.

        Parameters
        ----------
        kwargs: `dict`, optional
            Additional arguments which will be passed to the chat session.
            Refer to `gemini_webapi.ChatSession` for more information.

        Returns
        -------
        :class:`ChatSession`
            Empty chat session object for retrieving conversation history.
        """

        return ChatSession(geminiclient=self, **kwargs)


class ChatSession:
"""
Chat data to retrieve conversation history. Only if all 3 ids are provided will the conversation history be retrieved.

    Parameters
    ----------
    geminiclient: `GeminiClient`
        Async httpx client interface for gemini.google.com.
    metadata: `list[str]`, optional
        List of chat metadata `[cid, rid, rcid]`, can be shorter than 3 elements, like `[cid, rid]` or `[cid]` only.
    cid: `str`, optional
        Chat id, if provided together with metadata, will override the first value in it.
    rid: `str`, optional
        Reply id, if provided together with metadata, will override the second value in it.
    rcid: `str`, optional
        Reply candidate id, if provided together with metadata, will override the third value in it.
    model: `Model` | `str`, optional
        Specify the model to use for generation.
        Pass either a `gemini_webapi.constants.Model` enum or a model name string.
    gem: `Gem | str`, optional
        Specify a gem to use as system prompt for the chat session.
        Pass either a `gemini_webapi.types.Gem` object or a gem id string.
    """

    __slots__ = [
        "__metadata",
        "geminiclient",
        "last_output",
        "model",
        "gem",
    ]

    def __init__(
        self,
        geminiclient: GeminiClient,
        metadata: list[str | None] | None = None,
        cid: str | None = None,  # chat id
        rid: str | None = None,  # reply id
        rcid: str | None = None,  # reply candidate id
        model: Model | str = Model.UNSPECIFIED,
        gem: Gem | str | None = None,
    ):
        self.__metadata: list[str | None] = [None, None, None]
        self.geminiclient: GeminiClient = geminiclient
        self.last_output: ModelOutput | None = None
        self.model: Model | str = model
        self.gem: Gem | str | None = gem

        if metadata:
            self.metadata = metadata
        if cid:
            self.cid = cid
        if rid:
            self.rid = rid
        if rcid:
            self.rcid = rcid

    def __str__(self):
        return f"ChatSession(cid='{self.cid}', rid='{self.rid}', rcid='{self.rcid}')"

    __repr__ = __str__

    def __setattr__(self, name: str, value: Any) -> None:
        super().__setattr__(name, value)
        # update conversation history when last output is updated
        if name == "last_output" and isinstance(value, ModelOutput):
            self.metadata = value.metadata
            self.rcid = value.rcid

    async def send_message(
        self,
        prompt: str,
        files: list[str | Path] | None = None,
        **kwargs,
    ) -> ModelOutput:
        """
        Generates contents with prompt.
        Use as a shortcut for `GeminiClient.generate_content(prompt, image, self)`.

        Parameters
        ----------
        prompt: `str`
            Prompt provided by user.
        files: `list[str | Path]`, optional
            List of file paths to be attached.
        kwargs: `dict`, optional
            Additional arguments which will be passed to the post request.
            Refer to `httpx.AsyncClient.request` for more information.

        Returns
        -------
        :class:`ModelOutput`
            Output data from gemini.google.com, use `ModelOutput.text` to get the default text reply, `ModelOutput.images` to get a list
            of images in the default reply, `ModelOutput.candidates` to get a list of all answer candidates in the output.

        Raises
        ------
        `AssertionError`
            If prompt is empty.
        `gemini_webapi.TimeoutError`
            If request timed out.
        `gemini_webapi.GeminiError`
            If no reply candidate found in response.
        `gemini_webapi.APIError`
            - If request failed with status code other than 200.
            - If response structure is invalid and failed to parse.
        """

        return await self.geminiclient.generate_content(
            prompt=prompt,
            files=files,
            model=self.model,
            gem=self.gem,
            chat=self,
            **kwargs,
        )

    def choose_candidate(self, index: int) -> ModelOutput:
        """
        Choose a candidate from the last `ModelOutput` to control the ongoing conversation flow.

        Parameters
        ----------
        index: `int`
            Index of the candidate to choose, starting from 0.

        Returns
        -------
        :class:`ModelOutput`
            Output data of the chosen candidate.

        Raises
        ------
        `ValueError`
            If no previous output data found in this chat session, or if index exceeds the number of candidates in last model output.
        """

        if not self.last_output:
            raise ValueError("No previous output data found in this chat session.")

        if index >= len(self.last_output.candidates):
            raise ValueError(
                f"Index {index} exceeds the number of candidates in last model output."
            )

        self.last_output.chosen = index
        self.rcid = self.last_output.rcid
        return self.last_output

    @property
    def metadata(self):
        return self.__metadata

    @metadata.setter
    def metadata(self, value: list[str]):
        if len(value) > 3:
            raise ValueError("metadata cannot exceed 3 elements")
        self.__metadata[: len(value)] = value

    @property
    def cid(self):
        return self.__metadata[0]

    @cid.setter
    def cid(self, value: str):
        self.__metadata[0] = value

    @property
    def rid(self):
        return self.__metadata[1]

    @rid.setter
    def rid(self, value: str):
        self.__metadata[1] = value

    @property
    def rcid(self):
        return self.__metadata[2]

    @rcid.setter
    def rcid(self, value: str):
        self.__metadata[2] = value



================================================
FILE: src/gemini_webapi/constants.py
================================================
from enum import Enum, IntEnum, StrEnum


class Endpoint(StrEnum):
GOOGLE = "https://www.google.com"
INIT = "https://gemini.google.com/app"
GENERATE = "https://gemini.google.com/_/BardChatUi/data/assistant.lamda.BardFrontendService/StreamGenerate"
ROTATE_COOKIES = "https://accounts.google.com/RotateCookies"
UPLOAD = "https://content-push.googleapis.com/upload"
BATCH_EXEC = "https://gemini.google.com/_/BardChatUi/data/batchexecute"


class Headers(Enum):
GEMINI = {
"Content-Type": "application/x-www-form-urlencoded;charset=utf-8",
"Host": "gemini.google.com",
"Origin": "https://gemini.google.com",
"Referer": "https://gemini.google.com/",
"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
"X-Same-Domain": "1",
}
ROTATE_COOKIES = {
"Content-Type": "application/json",
}
UPLOAD = {"Push-ID": "feeds/mcudyrk2a4khkz"}


class Model(Enum):
UNSPECIFIED = ("unspecified", {}, False)
G_2_5_FLASH = (
"gemini-2.5-flash",
{"x-goog-ext-525001261-jspb": '[1,null,null,null,"71c2d248d3b102ff"]'},
False,
)
G_2_5_PRO = (
"gemini-2.5-pro",
{"x-goog-ext-525001261-jspb": '[1,null,null,null,"2525e3954d185b3c"]'},
False,
)
G_2_0_FLASH = (
"gemini-2.0-flash",
{"x-goog-ext-525001261-jspb": '[1,null,null,null,"f299729663a2343f"]'},
False,
)  # Deprecated
G_2_0_FLASH_THINKING = (
"gemini-2.0-flash-thinking",
{"x-goog-ext-525001261-jspb": '[null,null,null,null,"7ca48d02d802f20a"]'},
False,
)  # Deprecated

    def __init__(self, name, header, advanced_only):
        self.model_name = name
        self.model_header = header
        self.advanced_only = advanced_only

    @classmethod
    def from_name(cls, name: str):
        for model in cls:
            if model.model_name == name:
                return model
        raise ValueError(
            f"Unknown model name: {name}. Available models: {', '.join([model.model_name for model in cls])}"
        )


class ErrorCode(IntEnum):
"""
Known error codes returned from server.
"""

    USAGE_LIMIT_EXCEEDED = 1037
    MODEL_HEADER_INVALID = 1052
    IP_TEMPORARILY_BLOCKED = 1060



================================================
FILE: src/gemini_webapi/exceptions.py
================================================
class AuthError(Exception):
"""
Exception for authentication errors caused by invalid credentials/cookies.
"""

    pass


class APIError(Exception):
"""
Exception for package-level errors which need to be fixed in the future development (e.g. validation errors).
"""

    pass


class ImageGenerationError(APIError):
"""
Exception for generated image parsing errors.
"""

    pass


class GeminiError(Exception):
"""
Exception for errors returned from Gemini server which are not handled by the package.
"""

    pass


class TimeoutError(GeminiError):
"""
Exception for request timeouts.
"""

    pass


class UsageLimitExceeded(GeminiError):
"""
Exception for model usage limit exceeded errors.
"""

    pass


class ModelInvalid(GeminiError):
"""
Exception for invalid model header string errors.
"""

    pass


class TemporarilyBlocked(GeminiError):
"""
Exception for 429 Too Many Requests when IP is temporarily blocked.
"""

    pass



================================================
FILE: src/gemini_webapi/types/candidate.py
================================================
import html
from pydantic import BaseModel, field_validator

from .image import Image, WebImage, GeneratedImage


class Candidate(BaseModel):
"""
A single reply candidate object in the model output. A full response from Gemini usually contains multiple reply candidates.

    Parameters
    ----------
    rcid: `str`
        Reply candidate ID to build the metadata
    text: `str`
        Text output
    thoughts: `str`, optional
        Model's thought process, can be empty. Only populated with `-thinking` models
    web_images: `list[WebImage]`, optional
        List of web images in reply, can be empty.
    generated_images: `list[GeneratedImage]`, optional
        List of generated images in reply, can be empty
    """

    rcid: str
    text: str
    thoughts: str | None = None
    web_images: list[WebImage] = []
    generated_images: list[GeneratedImage] = []

    def __str__(self):
        return self.text

    def __repr__(self):
        return f"Candidate(rcid='{self.rcid}', text='{len(self.text) <= 20 and self.text or self.text[:20] + '...'}', images={self.images})"

    @field_validator("text", "thoughts")
    @classmethod
    def decode_html(cls, value: str) -> str:
        """
        Auto unescape HTML entities in text/thoughts if any.
        """

        if value:
            value = html.unescape(value)
        return value

    @property
    def images(self) -> list[Image]:
        return self.web_images + self.generated_images



================================================
FILE: src/gemini_webapi/types/gem.py
================================================
from pydantic import BaseModel


class Gem(BaseModel):
"""
Reusable Gemini Gem object working as a system prompt, providing additional context to the model.
Gemini provides a set of predefined gems, and users can create custom gems as well.

    Parameters
    ----------
    id: `str`
        Unique identifier for the gem.
    name: `str`
        User-friendly name of the gem.
    description: `str`, optional
        Brief description of the gem's purpose or content.
    prompt: `str`, optional
        The system prompt text that the gem provides to the model.
    predefined: `bool`
        Indicates whether the gem is predefined by Gemini or created by the user.
    """

    id: str
    name: str
    description: str | None = None
    prompt: str | None = None
    predefined: bool

    def __str__(self) -> str:
        return (
            f"Gem(id='{self.id}', name='{self.name}', description='{self.description}', "
            f"prompt='{self.prompt}', predefined={self.predefined})"
        )


class GemJar(dict[str, Gem]):
"""
Helper class for handling a collection of `Gem` objects, stored by their ID.
This class extends `dict` to allows retrieving gems with extra filtering options.
"""

    def __iter__(self):
        """
        Iter over the gems in the jar.
        """

        return self.values().__iter__()

    def get(
        self, id: str | None = None, name: str | None = None, default: Gem | None = None
    ) -> Gem | None:
        """
        Retrieves a gem by its id and/or name.
        If both id and name are provided, returns the gem that matches both id and name.
        If only id is provided, it's a direct lookup.
        If only name is provided, it searches through the gems.

        Parameters
        ----------
        id: `str`, optional
            The unique identifier of the gem to retrieve.
        name: `str`, optional
            The user-friendly name of the gem to retrieve.
        default: `Gem`, optional
            The default value to return if no matching gem is found.

        Returns
        -------
        `Gem` | None
            The matching gem if found, otherwise return the default value.

        Raises
        ------
        `AssertionError`
            If neither id nor name is provided.
        """

        assert not (
            id is None and name is None
        ), "At least one of gem id or name must be provided."

        if id is not None:
            gem_candidate = super().get(id)
            if gem_candidate:
                if name is not None:
                    if gem_candidate.name == name:
                        return gem_candidate
                    else:
                        return default
                else:
                    return gem_candidate
            else:
                return default
        elif name is not None:
            for gem_obj in self.values():
                if gem_obj.name == name:
                    return gem_obj
            return default

        # Should be unreachable due to the assertion.
        return default

    def filter(
        self, predefined: bool | None = None, name: str | None = None
    ) -> "GemJar":
        """
        Returns a new `GemJar` containing gems that match the given filters.

        Parameters
        ----------
        predefined: `bool`, optional
            If provided, filters gems by whether they are predefined (True) or user-created (False).
        name: `str`, optional
            If provided, filters gems by their name (exact match).

        Returns
        -------
        `GemJar`
            A new `GemJar` containing the filtered gems. Can be empty if no gems match the criteria.
        """

        filtered_gems = GemJar()

        for gem_id, gem in self.items():
            if predefined is not None and gem.predefined != predefined:
                continue
            if name is not None and gem.name != name:
                continue

            filtered_gems[gem_id] = gem

        return GemJar(filtered_gems)



================================================
FILE: src/gemini_webapi/types/image.py
================================================
import re
from pathlib import Path
from datetime import datetime

from httpx import AsyncClient, HTTPError
from pydantic import BaseModel, field_validator

from ..utils import logger


class Image(BaseModel):
"""
A single image object returned from Gemini.

    Parameters
    ----------
    url: `str`
        URL of the image.
    title: `str`, optional
        Title of the image, by default is "[Image]".
    alt: `str`, optional
        Optional description of the image.
    proxy: `str`, optional
        Proxy used when saving image.
    """

    url: str
    title: str = "[Image]"
    alt: str = ""
    proxy: str | None = None

    def __str__(self):
        return f"{self.title}({self.url}) - {self.alt}"

    def __repr__(self):
        return f"Image(title='{self.title}', url='{len(self.url) <= 20 and self.url or self.url[:8] + '...' + self.url[-12:]}', alt='{self.alt}')"

    async def save(
        self,
        path: str = "temp",
        filename: str | None = None,
        cookies: dict | None = None,
        verbose: bool = False,
        skip_invalid_filename: bool = False,
    ) -> str | None:
        """
        Save the image to disk.

        Parameters
        ----------
        path: `str`, optional
            Path to save the image, by default will save to "./temp".
        filename: `str`, optional
            File name to save the image, by default will use the original file name from the URL.
        cookies: `dict`, optional
            Cookies used for requesting the content of the image.
        verbose : `bool`, optional
            If True, will print the path of the saved file or warning for invalid file name, by default False.
        skip_invalid_filename: `bool`, optional
            If True, will only save the image if the file name and extension are valid, by default False.

        Returns
        -------
        `str | None`
            Absolute path of the saved image if successful, None if filename is invalid and `skip_invalid_filename` is True.

        Raises
        ------
        `httpx.HTTPError`
            If the network request failed.
        """

        filename = filename or self.url.split("/")[-1].split("?")[0]
        try:
            filename = re.search(r"^(.*\.\w+)", filename).group()
        except AttributeError:
            if verbose:
                logger.warning(f"Invalid filename: {filename}")
            if skip_invalid_filename:
                return None

        async with AsyncClient(
            http2=True, follow_redirects=True, cookies=cookies, proxy=self.proxy
        ) as client:
            response = await client.get(self.url)
            if response.status_code == 200:
                content_type = response.headers.get("content-type")
                if content_type and "image" not in content_type:
                    logger.warning(
                        f"Content type of {filename} is not image, but {content_type}."
                    )

                path = Path(path)
                path.mkdir(parents=True, exist_ok=True)

                dest = path / filename
                dest.write_bytes(response.content)

                if verbose:
                    logger.info(f"Image saved as {dest.resolve()}")

                return str(dest.resolve())
            else:
                raise HTTPError(
                    f"Error downloading image: {response.status_code} {response.reason_phrase}"
                )


class WebImage(Image):
"""
Image retrieved from web. Returned when ask Gemini to "SEND an image of [something]".
"""

    pass


class GeneratedImage(Image):
"""
Image generated by ImageFX, Google's AI image generator. Returned when ask Gemini to "GENERATE an image of [something]".

    Parameters
    ----------
    cookies: `dict`
        Cookies used for requesting the content of the generated image, inherit from GeminiClient object or manually set.
        Should contain valid "__Secure-1PSID" and "__Secure-1PSIDTS" values.
    """

    cookies: dict[str, str]

    @field_validator("cookies")
    @classmethod
    def validate_cookies(cls, v: dict) -> dict:
        if len(v) == 0:
            raise ValueError(
                "GeneratedImage is designed to be initialized with same cookies as GeminiClient."
            )
        return v

    # @override
    async def save(self, full_size=True, **kwargs) -> str | None:
        """
        Save the image to disk.

        Parameters
        ----------
        filename: `str`, optional
            Filename to save the image, generated images are always in .png format, but file extension will not be included in the URL.
            And since the URL ends with a long hash, by default will use timestamp + end of the hash as the filename.
        full_size: `bool`, optional
            If True, will modify the default preview (512*512) URL to get the full size image.
        kwargs: `dict`, optional
            Other arguments to pass to `Image.save`.

        Returns
        -------
        `str | None`
            Absolute path of the saved image if successfully saved.
        """

        if full_size:
            self.url += "=s2048"

        return await super().save(
            filename=kwargs.pop("filename", None)
            or f"{datetime.now().strftime('%Y%m%d%H%M%S')}_{self.url[-10:]}.png",
            cookies=self.cookies,
            **kwargs,
        )



================================================
FILE: src/gemini_webapi/types/modeloutput.py
================================================
from pydantic import BaseModel

from .image import Image
from .candidate import Candidate


class ModelOutput(BaseModel):
"""
Classified output from gemini.google.com

    Parameters
    ----------
    metadata: `list[str]`
        List of chat metadata `[cid, rid, rcid]`, can be shorter than 3 elements, like `[cid, rid]` or `[cid]` only
    candidates: `list[Candidate]`
        List of all candidates returned from gemini
    chosen: `int`, optional
        Index of the chosen candidate, by default will choose the first one
    """

    metadata: list[str]
    candidates: list[Candidate]
    chosen: int = 0

    def __str__(self):
        return self.text

    def __repr__(self):
        return f"ModelOutput(metadata={self.metadata}, chosen={self.chosen}, candidates={self.candidates})"

    @property
    def text(self) -> str:
        return self.candidates[self.chosen].text

    @property
    def thoughts(self) -> str | None:
        return self.candidates[self.chosen].thoughts

    @property
    def images(self) -> list[Image]:
        return self.candidates[self.chosen].images

    @property
    def rcid(self) -> str:
        return self.candidates[self.chosen].rcid



================================================
FILE: src/gemini_webapi/utils/get_access_token.py
================================================
import re
import asyncio
from asyncio import Task
from pathlib import Path

from httpx import AsyncClient, Response

from ..constants import Endpoint, Headers
from ..exceptions import AuthError
from .load_browser_cookies import load_browser_cookies
from .logger import logger


async def send_request(
cookies: dict, proxy: str | None = None
) -> tuple[Response | None, dict]:
"""
Send http request with provided cookies.
"""

    async with AsyncClient(
        http2=True,
        proxy=proxy,
        headers=Headers.GEMINI.value,
        cookies=cookies,
        follow_redirects=True,
        verify=False,
    ) as client:
        response = await client.get(Endpoint.INIT.value)
        response.raise_for_status()
        return response, cookies


async def get_access_token(
base_cookies: dict, proxy: str | None = None, verbose: bool = False
) -> tuple[str, dict]:
"""
Send a get request to gemini.google.com for each group of available cookies and return
the value of "SNlM0e" as access token on the first successful request.

    Possible cookie sources:
    - Base cookies passed to the function.
    - __Secure-1PSID from base cookies with __Secure-1PSIDTS from cache.
    - Local browser cookies (if optional dependency `browser-cookie3` is installed).

    Parameters
    ----------
    base_cookies : `dict`
        Base cookies to be used in the request.
    proxy: `str`, optional
        Proxy URL.
    verbose: `bool`, optional
        If `True`, will print more infomation in logs.

    Returns
    -------
    `str`
        Access token.
    `dict`
        Cookies of the successful request.

    Raises
    ------
    `gemini_webapi.AuthError`
        If all requests failed.
    """

    async with AsyncClient(
        http2=True,
        proxy=proxy,
        follow_redirects=True,
        verify=False,
    ) as client:
        response = await client.get(Endpoint.GOOGLE.value)

    extra_cookies = {}
    if response.status_code == 200:
        extra_cookies = response.cookies

    tasks = []

    # Base cookies passed directly on initializing client
    if "__Secure-1PSID" in base_cookies and "__Secure-1PSIDTS" in base_cookies:
        tasks.append(Task(send_request({**extra_cookies, **base_cookies}, proxy=proxy)))
    elif verbose:
        logger.debug(
            "Skipping loading base cookies. Either __Secure-1PSID or __Secure-1PSIDTS is not provided."
        )

    # Cached cookies in local file
    cache_dir = Path(__file__).parent / "temp"
    if "__Secure-1PSID" in base_cookies:
        filename = f".cached_1psidts_{base_cookies['__Secure-1PSID']}.txt"
        cache_file = cache_dir / filename
        if cache_file.is_file():
            cached_1psidts = cache_file.read_text()
            if cached_1psidts:
                cached_cookies = {
                    **extra_cookies,
                    **base_cookies,
                    "__Secure-1PSIDTS": cached_1psidts,
                }
                tasks.append(Task(send_request(cached_cookies, proxy=proxy)))
            elif verbose:
                logger.debug("Skipping loading cached cookies. Cache file is empty.")
        elif verbose:
            logger.debug("Skipping loading cached cookies. Cache file not found.")
    else:
        valid_caches = 0
        cache_files = cache_dir.glob(".cached_1psidts_*.txt")
        for cache_file in cache_files:
            cached_1psidts = cache_file.read_text()
            if cached_1psidts:
                cached_cookies = {
                    **extra_cookies,
                    "__Secure-1PSID": cache_file.stem[16:],
                    "__Secure-1PSIDTS": cached_1psidts,
                }
                tasks.append(Task(send_request(cached_cookies, proxy=proxy)))
                valid_caches += 1

        if valid_caches == 0 and verbose:
            logger.debug(
                "Skipping loading cached cookies. Cookies will be cached after successful initialization."
            )

    # Browser cookies (if browser-cookie3 is installed)
    try:
        browser_cookies = load_browser_cookies(
            domain_name="google.com", verbose=verbose
        )
        if browser_cookies and (secure_1psid := browser_cookies.get("__Secure-1PSID")):
            local_cookies = {"__Secure-1PSID": secure_1psid}
            if secure_1psidts := browser_cookies.get("__Secure-1PSIDTS"):
                local_cookies["__Secure-1PSIDTS"] = secure_1psidts
            if nid := browser_cookies.get("NID"):
                local_cookies["NID"] = nid
            tasks.append(Task(send_request(local_cookies, proxy=proxy)))
        elif verbose:
            logger.debug(
                "Skipping loading local browser cookies. Login to gemini.google.com in your browser first."
            )
    except ImportError:
        if verbose:
            logger.debug(
                "Skipping loading local browser cookies. Optional dependency 'browser-cookie3' is not installed."
            )
    except Exception as e:
        if verbose:
            logger.warning(f"Skipping loading local browser cookies. {e}")

    for i, future in enumerate(asyncio.as_completed(tasks)):
        try:
            response, request_cookies = await future
            match = re.search(r'"SNlM0e":"(.*?)"', response.text)
            if match:
                if verbose:
                    logger.debug(
                        f"Init attempt ({i + 1}/{len(tasks)}) succeeded. Initializing client..."
                    )
                return match.group(1), request_cookies
            elif verbose:
                logger.debug(
                    f"Init attempt ({i + 1}/{len(tasks)}) failed. Cookies invalid."
                )
        except Exception as e:
            if verbose:
                logger.debug(
                    f"Init attempt ({i + 1}/{len(tasks)}) failed with error: {e}"
                )

    raise AuthError(
        "Failed to initialize client. SECURE_1PSIDTS could get expired frequently, please make sure cookie values are up to date. "
        f"(Failed initialization attempts: {len(tasks)})"
    )



================================================
FILE: src/gemini_webapi/utils/load_browser_cookies.py
================================================
from .logger import logger


def load_browser_cookies(domain_name: str = "", verbose=True) -> dict:
"""
Try to load cookies from all supported browsers and return combined cookiejar.
Optionally pass in a domain name to only load cookies from the specified domain.

    Parameters
    ----------
    domain_name : str, optional
        Domain name to filter cookies by, by default will load all cookies without filtering.
    verbose : bool, optional
        If `True`, will print more infomation in logs.

    Returns
    -------
    `dict`
        Dictionary with cookie name as key and cookie value as value.
    """

    import browser_cookie3 as bc3

    cookies = {}
    for cookie_fn in [
        bc3.chrome,
        bc3.chromium,
        bc3.opera,
        bc3.opera_gx,
        bc3.brave,
        bc3.edge,
        bc3.vivaldi,
        bc3.firefox,
        bc3.librewolf,
        bc3.safari,
    ]:
        try:
            for cookie in cookie_fn(domain_name=domain_name):
                cookies[cookie.name] = cookie.value
        except bc3.BrowserCookieError:
            pass
        except PermissionError as e:
            if verbose:
                logger.warning(
                    f"Permission denied while trying to load cookies from {cookie_fn.__name__}. {e}"
                )
        except Exception as e:
            if verbose:
                logger.error(
                    f"Error happened while trying to load cookies from {cookie_fn.__name__}. {e}"
                )

    return cookies



================================================
FILE: src/gemini_webapi/utils/logger.py
================================================
import sys
from loguru import logger as _logger

_handler_id = None


def set_log_level(level: str | int) -> None:
"""
Set the log level for gemini_webapi. The default log level is "INFO".

    Note: calling this function for the first time will globally remove all existing loguru
    handlers. To avoid this, you may want to set logging behaviors directly with loguru.

    Parameters
    ----------
    level : `str | int`
        Log level: "TRACE", "DEBUG", "INFO", "WARNING", "ERROR", "CRITICAL"

    Examples
    --------
    >>> from gemini_webapi import set_log_level
    >>> set_log_level("DEBUG")  # Show debug messages
    >>> set_log_level("ERROR")  # Only show errors
    """

    global _handler_id

    _logger.remove(_handler_id)

    _handler_id = _logger.add(
        sys.stderr,
        level=level,
        filter=lambda record: record["extra"].get("name") == "gemini_webapi",
    )


logger = _logger.bind(name="gemini_webapi")



================================================
FILE: src/gemini_webapi/utils/rotate_1psidts.py
================================================
import os
import time
from pathlib import Path

from httpx import AsyncClient

from ..constants import Endpoint, Headers
from ..exceptions import AuthError


async def rotate_1psidts(cookies: dict, proxy: str | None = None) -> str:
"""
Refresh the __Secure-1PSIDTS cookie and store the refreshed cookie value in cache file.

    Parameters
    ----------
    cookies : `dict`
        Cookies to be used in the request.
    proxy: `str`, optional
        Proxy URL.

    Returns
    -------
    `str`
        New value of the __Secure-1PSIDTS cookie.

    Raises
    ------
    `gemini_webapi.AuthError`
        If request failed with 401 Unauthorized.
    `httpx.HTTPStatusError`
        If request failed with other status codes.
    """

    path = Path(__file__).parent / "temp"
    path.mkdir(parents=True, exist_ok=True)
    filename = f".cached_1psidts_{cookies['__Secure-1PSID']}.txt"
    path = path / filename

    # Check if the cache file was modified in the last minute to avoid 429 Too Many Requests
    if not (path.is_file() and time.time() - os.path.getmtime(path) <= 60):
        async with AsyncClient(http2=True, proxy=proxy) as client:
            response = await client.post(
                url=Endpoint.ROTATE_COOKIES.value,
                headers=Headers.ROTATE_COOKIES.value,
                cookies=cookies,
                data='[000,"-0000000000000000000"]',
            )
            if response.status_code == 401:
                raise AuthError
            response.raise_for_status()

            if new_1psidts := response.cookies.get("__Secure-1PSIDTS"):
                path.write_text(new_1psidts)
                return new_1psidts



================================================
FILE: src/gemini_webapi/utils/upload_file.py
================================================
from pathlib import Path

from httpx import AsyncClient
from pydantic import validate_call

from ..constants import Endpoint, Headers


@validate_call
async def upload_file(file: str | Path, proxy: str | None = None) -> str:
"""
Upload a file to Google's server and return its identifier.

    Parameters
    ----------
    file : `str` | `Path`
        Path to the file to be uploaded.
    proxy: `str`, optional
        Proxy URL.

    Returns
    -------
    `str`
        Identifier of the uploaded file.
        E.g. "/contrib_service/ttl_1d/1709764705i7wdlyx3mdzndme3a767pluckv4flj"

    Raises
    ------
    `httpx.HTTPStatusError`
        If the upload request failed.
    """

    with open(file, "rb") as f:
        file = f.read()

    async with AsyncClient(http2=True, proxy=proxy) as client:
        response = await client.post(
            url=Endpoint.UPLOAD.value,
            headers=Headers.UPLOAD.value,
            files={"file": file},
            follow_redirects=True,
        )
        response.raise_for_status()
        return response.text


def parse_file_name(file: str | Path) -> str:
"""
Parse the file name from the given path.

    Parameters
    ----------
    file : `str` | `Path`
        Path to the file.

    Returns
    -------
    `str`
        File name with extension.
    """

    file = Path(file)
    if not file.is_file():
        raise ValueError(f"{file} is not a valid file.")

    return file.name


