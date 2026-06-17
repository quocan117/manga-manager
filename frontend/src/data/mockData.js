export const categories = [
  "Hành động",
  "Phiêu lưu",
  "Hài hước",
  "Tình cảm",
  "Siêu nhiên",
  "Kinh dị",
  "Học đường",
  "Lịch sử",
  "Âm nhạc",
  "Phép thuật",
  "Thể thao",
  "Movie",
  "Đời thường",
  "Huyền bí",
  "Võ thuật",
  "Trinh thám",
  "Tâm lý",
  "Game",
  "Giả tưởng",
];

const initialSeries = [
  {
    id: 1,
    title: "Vua Hải Tặc",
    author: "Eiichiro Oda",
    genres: ["Hành động", "Phiêu lưu", "Hài hước"],
    coverUrl: "https://vov2.vov.vn/sites/default/files/images/vuahaitac.jpg",
    description:
      "Câu chuyện về kỷ nguyên hải tặc vĩ đại. Cùng bình chọn cho chặng đường cuối cùng của Vua Hải Tặc!",
    status: "Published",
    chapters: [
      { id: 101, title: "Chương 1111: Tứ Hoàng Hành Động", votes: 340 },
      { id: 102, title: "Chương 1110: Tinh Tú Giáng Lâm", votes: 325 },
    ],
  },
  {
    id: 2,
    title: "Chú Thuật Hồi Chiến",
    author: "Gege Akutami",
    genres: ["Hành động", "Siêu nhiên", "Kinh dị"],
    coverUrl: "https://i.redd.it/dk4x6yay4grg1.jpeg",
    description:
      "Cuộc chiến khốc liệt giữa các chú thuật sư. Ai sẽ là người sống sót cuối cùng?",
    status: "Reviewing",
    chapters: [
      { id: 201, title: "Chương 256: Hắc Thiểm", votes: 901 },
      { id: 202, title: "Chương 255: Quyết Chiến Trận Địa", votes: 850 },
    ],
  },
  {
    id: 3,
    title: "Thanh Gươm Diệt Quỷ",
    author: "Koyoharu Gotouge",
    genres: ["Hành động", "Lịch sử", "Siêu nhiên"],
    coverUrl: "https://placehold.co/200x280/55efc4/ffffff?text=Demon+Slayer",
    description:
      "Hành trình diệt quỷ bảo vệ em gái của Tanjirou. Hãy vote cho những phân cảnh cảm động nhất.",
    status: "Draft",
    chapters: [
      { id: 301, title: "Chương 205: Tương Lai Tuyệt Vời", votes: 219 },
      { id: 302, title: "Chương 204: Thế Giới Không Có Quỷ", votes: 198 },
    ],
  },
  {
    id: 4,
    title: "Thám Tử Lừng Danh Conan",
    author: "Gosho Aoyama",
    genres: ["Trinh thám", "Học đường", "Hài hước"],
    coverUrl: "https://placehold.co/200x280/ffeaa7/000000?text=Conan",
    description:
      "Hành trình phá án tìm lại thân xác thực sự của thám tử học sinh Kudo Shinichi.",
    status: "Published",
    chapters: [
      { id: 401, title: "Chương 1120: Vụ Án Ở Đền Thờ", votes: 450 },
      { id: 402, title: "Chương 1119: Lời Giải Chiếc Chìa Khóa", votes: 410 },
    ],
  },
  {
    id: 5,
    title: "Đại Chiến Titan",
    author: "Hajime Isayama",
    genres: ["Hành động", "Tâm lý", "Giả tưởng"],
    coverUrl: "https://placehold.co/200x280/fab1a0/000000?text=AoT",
    description:
      "Cuộc chiến sinh tồn tàn khốc của nhân loại trước những thực thể Titan khổng lồ khát máu.",
    status: "Published",
    chapters: [
      { id: 501, title: "Chương 139: Bầu Trời Tự Do", votes: 1200 },
      { id: 502, title: "Chương 138: Giấc Mơ Dài", votes: 1150 },
    ],
  },
  {
    id: 6,
    title: "Haikyu!! Vua Bóng Chuyền",
    author: "Haruichi Furudate",
    genres: ["Thể thao", "Học đường", "Hài hước"],
    coverUrl: "https://placehold.co/200x280/fdcb6e/000000?text=Haikyu",
    description:
      "Hành trình chinh phục đỉnh cao giải bóng chuyền cao trung của Hinata Shoyo cùng đội Karasuno.",
    status: "Published",
    chapters: [
      { id: 601, title: "Chương 402: Trận Chiến Cuối Cùng", votes: 670 },
      { id: 602, title: "Chương 401: Ngôi Sao Tỏa Sáng", votes: 610 },
    ],
  },
  {
    id: 7,
    title: "Naruto: Cửu Vĩ Hồ",
    author: "Masashi Kishimoto",
    genres: ["Hành động", "Phiêu lưu", "Võ thuật"],
    coverUrl: "https://placehold.co/200x280/e17055/ffffff?text=Naruto",
    description:
      "Ước mơ trở thành Hokage vĩ đại nhất làng Lá của cậu bé mang trong mình phong ấn Cửu Vĩ.",
    status: "Published",
    chapters: [
      { id: 701, title: "Chương 700: Uzumaki Naruto!!", votes: 890 },
      { id: 702, title: "Chương 699: Dấu Ấn Hòa Bình", votes: 820 },
    ],
  },
  {
    id: 8,
    title: "Hội Pháp Sư Fairy Tail",
    author: "Hiro Mashima",
    genres: ["Phép thuật", "Phiêu lưu", "Hài hước"],
    coverUrl: "https://placehold.co/200x280/9b59b6/ffffff?text=Fairy+Tail",
    description:
      "Những cuộc phiêu lưu đầy phép thuật, tình đồng đội của Natsu, Lucy và hội Fairy Tail.",
    status: "Published",
    chapters: [
      { id: 801, title: "Chương 545: Những Người Bạn Tri Kỷ", votes: 310 },
      { id: 802, title: "Chương 544: Sức Mạnh Của Cảm Xúc", votes: 295 },
    ],
  },
  {
    id: 9,
    title: "Solo Leveling: Tôi Thăng Cấp Một Mình",
    author: "Chugong",
    genres: ["Hành động", "Game", "Huyền bí"],
    coverUrl: "https://placehold.co/200x280/34495e/ffffff?text=Solo+Leveling",
    description:
      "Từ thợ săn yếu nhất thế giới, Sung Jin-Woo có được khả năng thăng cấp vô hạn thông qua hệ thống bí ẩn.",
    status: "Published",
    chapters: [
      { id: 901, title: "Chương 179: Tân Vương Trỗi Dậy", votes: 1450 },
      { id: 902, title: "Chương 178: Trận Chiến Bóng Đêm", votes: 1390 },
    ],
  },
  {
    id: 10,
    title: "Dáng Hình Thanh Âm",
    author: "Yoshitoki Ōima",
    genres: ["Tâm lý", "Học đường", "Đời thường"],
    coverUrl: "https://placehold.co/200x280/e84393/ffffff?text=A+Silent+Voice",
    description:
      "Câu chuyện cảm động về sự chuộc lỗi, thấu hiểu và kết nối giữa một cậu bé ngỗ nghịch và cô bé khiếm thính.",
    status: "Published",
    chapters: [
      { id: 1001, title: "Chương 62: Đi Về Phía Ánh Sáng", votes: 520 },
      { id: 1002, title: "Chương 61: Lời Xin Lỗi Chân Thành", votes: 490 },
    ],
  },
];

const generatedSeries = Array.from({ length: 40 }, (_, index) => {
  const id = index + 11;
  const colorList = [
    "ff7675",
    "74b9ff",
    "55efc4",
    "ffeaa7",
    "fab1a0",
    "fdcb6e",
    "e17055",
    "9b59b6",
    "34495e",
    "e84393",
  ];
  const bgColor = colorList[index % colorList.length];

  return {
    id: id,
    title: `Manga Series Khám Phá #${id}`,
    author: `Mangaka Ẩn Danh ${id}`,
    genres: [categories[index % categories.length], "Hài hước"],
    coverUrl: `https://placehold.co/200x280/${bgColor}/ffffff?text=Manga+${id}`,
    description: `Đây là dữ liệu truyện tranh giả lập số ${id} dùng để test tính năng phân trang.`,
    status: id % 2 === 0 ? "Published" : "Draft",
    chapters: [
      { id: id * 100 + 1, title: `Chương 2`, votes: Math.floor(id * 1.5) },
      { id: id * 100 + 2, title: `Chương 1`, votes: Math.floor(id * 2.5) },
    ],
  };
});
export const trendingSeries = [...initialSeries, ...generatedSeries];

export const pendingSeriesData = [
  {
    id: 1001,
    title: "Kẻ Săn Bóng Đêm",
    author: "Nguyễn Minh T",
    genre: "Kinh dị, Hành động",
    submissionDate: "2026-06-15",
    description: "Một thợ săn quỷ vô tình bị dính lời nguyền hắc ám...",
    status: "Pending"
  },
  {
    id: 1002,
    title: "Học Viện Pháp Thuật Ánh Sáng",
    author: "Trần Lê V",
    genre: "Fantasy, Học đường",
    submissionDate: "2026-06-16",
    description: "Hành trình vươn lên của một pháp sư không có năng lực bẩm sinh.",
    status: "Pending"
  }
];

export const pendingMangakaData = [
  {
    id: 2001,
    name: "Lê Hoàng A",
    email: "hoanga@example.com",
    portfolioUrl: "https://behance.net/hoanga",
    applyDate: "2026-06-14",
    status: "Pending"
  }
];

export const rankingData = [
  { id: 1, title: "Vua Hải Tặc", author: "Eiichiro Oda", totalVotes: 15400, rank: 1, trend: "up" },
  { id: 2, title: "Đại Chiến Titan", author: "Hajime Isayama", totalVotes: 14200, rank: 2, trend: "same" },
  { id: 3, title: "Chú Thuật Hồi Chiến", author: "Gege Akutami", totalVotes: 8900, rank: 3, trend: "down" },
  { id: 4, title: "Manga Khám Phá #45", author: "Mangaka Ẩn Danh 45", totalVotes: 120, rank: 45, trend: "danger" } 
];