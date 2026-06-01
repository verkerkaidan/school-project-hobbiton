#[derive(Debug, Clone, Default)]
pub struct Book {
    pub id: Option<String>,
    pub title: Option<String>,
    pub authors: Vec<String>,
    pub published_date: Option<String>,
    pub publishers: Vec<String>,
    pub isbn: Vec<String>,
    pub page_count: Option<u32>,
    pub languages: Vec<String>,
    pub thumbnail_url: Option<String>,
    pub cover_id: Option<String>,
}