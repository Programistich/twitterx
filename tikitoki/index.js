const { downloadTiktok } = require("@mrnima/tiktok-downloader")
const express = require('express')

const app = express()
const port = 3000

app.get('/tiktok', async (req, res) => {    try {
    const url = req.query.url;
    if (!url) {
        return res.status(400).json({
            status: false,
            message: 'Missing "url" query parameter'
        });
    }

    const originalResult = await downloadTiktok(url);

    // Basic checks to ensure we have the fields we expect
    if (!originalResult?.result?.dl_link) {
        return res.status(400).json({
            status: false,
            message: 'Invalid result structure'
        });
    }

    const { dl_link } = originalResult.result;

    // Decide which type of data to return
    if (Array.isArray(dl_link.images) && dl_link.images.length > 0) {
        // Return multiple images (type-images)
        return res.json({
            type: 'images',
            photo: dl_link.images  // Expecting an array of image URLs
        });
    } else {
        // Return video (type-video). Choose the link you want (mp4_1, mp4_2, or mp4_hd).
        return res.json({
            type: 'video',
            video: dl_link.download_mp4_1 || dl_link.download_mp4_2 || dl_link.download_mp4_hd
        });
    }

    } catch (err) {
        console.error(err);
        return res.status(500).json({
            status: false,
            message: 'Internal server error'
        });
    }
});


app.listen(port, () => {
    console.log(`Example app listening on port ${port}`)
})
